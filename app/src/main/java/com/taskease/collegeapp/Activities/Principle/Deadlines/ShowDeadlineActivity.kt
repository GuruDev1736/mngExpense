package com.taskease.collegeapp.Activities.Principle.Deadlines

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.taskease.collegeapp.Activities.Faculty.Workload.AddWorkLoadReport
import com.taskease.collegeapp.Adapter.DeadLineAdapter
import com.taskease.collegeapp.Adapter.SpreadSheetAdapter
import com.taskease.collegeapp.Constants.Constant
import com.taskease.collegeapp.Constants.CustomProgressDialog
import com.taskease.collegeapp.Constants.SharedPreferencesManager
import com.taskease.collegeapp.Model.Response.GetAllDeadlineResponseModel
import com.taskease.collegeapp.Model.Response.SpreadSheetResponseModel
import com.taskease.collegeapp.R
import com.taskease.collegeapp.Retrofit.RetrofitInstance
import com.taskease.collegeapp.databinding.ActivityShowDeadlineBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ShowDeadlineActivity : AppCompatActivity() {

    private lateinit var binding : ActivityShowDeadlineBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowDeadlineBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.actionBar.back.setOnClickListener{
            finish()
        }
        binding.actionBar.title.text = "Deadline Reports"

        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        binding.addSpreadSheet.setOnClickListener{
            startActivity(Intent(this, AddDeadlineActivity::class.java))
        }
    }


    override fun onStart() {
        super.onStart()
        CallGetAllDeadlines()
    }

    private fun CallGetAllDeadlines() {
        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()

        try {
            val token = SharedPreferencesManager.getString(this, SharedPreferencesManager.TOKEN)

            RetrofitInstance.getInstance().getAllDeadlines(token)
                .enqueue(object : Callback<GetAllDeadlineResponseModel> {
                    override fun onResponse(
                        p0: Call<GetAllDeadlineResponseModel>,
                        p1: Response<GetAllDeadlineResponseModel>
                    ) {
                        if (p1.isSuccessful)
                        {
                            progressDialog.dismiss()
                            val data = p1.body()
                            if (data!=null)
                            {
                                if (data.STS == "200")
                                {
                                    if (data.CONTENT.isEmpty())
                                    {
                                        Constant.toast(this@ShowDeadlineActivity,"No Data Found")
                                        binding.noData.visibility = View.VISIBLE
                                    }
                                    else
                                    {
                                        binding.noData.visibility = View.GONE
                                    }
                                    val adapter = DeadLineAdapter(this@ShowDeadlineActivity,data.CONTENT)
                                    binding.recyclerView.adapter = adapter
                                }
                                else
                                {
                                    Constant.toast(this@ShowDeadlineActivity,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@ShowDeadlineActivity,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@ShowDeadlineActivity,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<GetAllDeadlineResponseModel>, p1: Throwable) {
                        Constant.toast(this@ShowDeadlineActivity,p1.message.toString())
                        progressDialog.dismiss()
                    }

                })
        }catch (e : Exception)
        {
            Constant.toast(this,"Something went wrong")
            e.printStackTrace()
            progressDialog.dismiss()
        }


    }
}