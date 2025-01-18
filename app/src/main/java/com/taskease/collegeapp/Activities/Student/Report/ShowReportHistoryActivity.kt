package com.taskease.collegeapp.Activities.Student.Report

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.taskease.collegeapp.Adapter.ReportAdapter
import com.taskease.collegeapp.Constants.Constant
import com.taskease.collegeapp.Constants.CustomProgressDialog
import com.taskease.collegeapp.Constants.SharedPreferencesManager
import com.taskease.collegeapp.Model.Response.GetAllReportResponseModel
import com.taskease.collegeapp.R
import com.taskease.collegeapp.Retrofit.RetrofitInstance
import com.taskease.collegeapp.databinding.ActivityShowReportHistoryBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ShowReportHistoryActivity : AppCompatActivity() {

    private lateinit var binding : ActivityShowReportHistoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowReportHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.actionBar.title.text = "Lost Report"
        binding.actionBar.back.setOnClickListener{
            finish()
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
    }

    override fun onStart() {
        super.onStart()
        getAllReports()
    }

    private fun getAllReports() {
        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()

        try {
            val token = SharedPreferencesManager.getString(this, SharedPreferencesManager.TOKEN)
            val userId = SharedPreferencesManager.getInt(this, SharedPreferencesManager.USERID)

            RetrofitInstance.getInstance().getAllReportByStudent(token,userId)
                .enqueue(object : Callback<GetAllReportResponseModel> {
                    override fun onResponse(
                        p0: Call<GetAllReportResponseModel>,
                        p1: Response<GetAllReportResponseModel>
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
                                        Constant.toast(this@ShowReportHistoryActivity,"No Data Found")
                                        binding.noData.visibility = View.VISIBLE
                                    }
                                    else
                                    {
                                        binding.noData.visibility = View.GONE
                                    }
                                    val adapter = ReportAdapter(this@ShowReportHistoryActivity,data.CONTENT.toMutableList(),"HISTORY")
                                    binding.recyclerView.adapter = adapter
                                }
                                else
                                {
                                    Constant.toast(this@ShowReportHistoryActivity,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@ShowReportHistoryActivity,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@ShowReportHistoryActivity,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<GetAllReportResponseModel>, p1: Throwable) {
                        Constant.toast(this@ShowReportHistoryActivity,p1.message.toString())
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