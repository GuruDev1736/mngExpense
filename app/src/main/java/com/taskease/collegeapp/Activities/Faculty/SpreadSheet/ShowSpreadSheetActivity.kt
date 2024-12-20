package com.taskease.collegeapp.Activities.Faculty.SpreadSheet

import android.content.Intent
import android.os.Bundle
import android.view.View
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
import com.taskease.collegeapp.databinding.ActivityShowSpreadSheetBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ShowSpreadSheetActivity : AppCompatActivity() {

    private lateinit var binding : ActivityShowSpreadSheetBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowSpreadSheetBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.actionBar.back.setOnClickListener{
            finish()
        }
        binding.actionBar.title.text = "Spread Sheet"

        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        binding.addSpreadSheet.setOnClickListener{
            startActivity(Intent(this,SpreadSheetActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()

        CallGetAllSpreadSheet()
    }

    private fun CallGetAllSpreadSheet() {
        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()

        val token = SharedPreferencesManager.getString(this, SharedPreferencesManager.TOKEN)
        val departmentId = SharedPreferencesManager.getInt(this, SharedPreferencesManager.DEPARTMENTID)

        RetrofitInstance.getInstance().getAllSpreadSheet(token,"SPREADSHEET",departmentId)
            .enqueue(object : Callback<SpreadSheetResponseModel>{
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
                                    Constant.toast(this@ShowSpreadSheetActivity,"No Data Found")
                                    binding.noData.visibility = View.VISIBLE
                                }
                                val adapter = SpreadSheetAdapter(this@ShowSpreadSheetActivity,data.CONTENT)
                                binding.recyclerView.adapter = adapter
                            }
                            else
                            {
                                Constant.toast(this@ShowSpreadSheetActivity,data.MSG)
                            }
                        }
                        else{
                            Constant.toast(this@ShowSpreadSheetActivity,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }
                    else{
                        Constant.toast(this@ShowSpreadSheetActivity,p1.message().toString())
                        progressDialog.dismiss()
                    }
                }

                override fun onFailure(p0: Call<SpreadSheetResponseModel>, p1: Throwable) {
                    Constant.toast(this@ShowSpreadSheetActivity,p1.message.toString())
                    progressDialog.dismiss()
                }

            })
    }
}