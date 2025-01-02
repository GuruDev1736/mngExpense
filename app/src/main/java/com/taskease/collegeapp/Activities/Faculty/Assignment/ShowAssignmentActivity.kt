package com.taskease.collegeapp.Activities.Faculty.Assignment

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.taskease.collegeapp.Adapter.SpreadSheetAdapter
import com.taskease.collegeapp.Constants.Constant
import com.taskease.collegeapp.Constants.CustomProgressDialog
import com.taskease.collegeapp.Constants.SharedPreferencesManager
import com.taskease.collegeapp.Model.Response.SpreadSheetResponseModel
import com.taskease.collegeapp.R
import com.taskease.collegeapp.Retrofit.RetrofitInstance
import com.taskease.collegeapp.databinding.ActivityShowAssignmentBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ShowAssignmentActivity : AppCompatActivity() {

    private lateinit var binding : ActivityShowAssignmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowAssignmentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.actionBar.back.setOnClickListener{
            finish()
        }
        binding.actionBar.title.text = "Assignments"

        binding.addSpreadSheet.setOnClickListener{
            startActivity(Intent(this,AddAssignmentActivity::class.java))
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)


    }

    override fun onStart() {
        super.onStart()
        CallGetAllAssignment()
    }

    private fun CallGetAllAssignment() {
        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()

        try {
            val token = SharedPreferencesManager.getString(this, SharedPreferencesManager.TOKEN)
            val departmentId = SharedPreferencesManager.getInt(this, SharedPreferencesManager.DEPARTMENTID)
            val userId = SharedPreferencesManager.getInt(this, SharedPreferencesManager.USERID)

            RetrofitInstance.getInstance().getAllSpreadSheetByUser(token,"ASSIGNMENT",departmentId,userId)
                .enqueue(object : Callback<SpreadSheetResponseModel> {
                    override fun onResponse(
                        p0: Call<SpreadSheetResponseModel>,
                        p1: Response<SpreadSheetResponseModel>
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
                                        Constant.toast(this@ShowAssignmentActivity,"No Data Found")
                                        binding.noData.visibility = View.VISIBLE
                                    }
                                    else
                                    {
                                        binding.noData.visibility = View.GONE
                                    }
                                    val adapter = SpreadSheetAdapter(this@ShowAssignmentActivity,data.CONTENT.toMutableList(),"ASSIGNMENT")
                                    binding.recyclerView.adapter = adapter
                                }
                                else
                                {
                                    Constant.toast(this@ShowAssignmentActivity,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@ShowAssignmentActivity,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@ShowAssignmentActivity,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<SpreadSheetResponseModel>, p1: Throwable) {
                        Constant.toast(this@ShowAssignmentActivity,p1.message.toString())
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