package com.taskease.collegeapp.Activities.Student

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.taskease.collegeapp.Adapter.SpreadSheetAdapter
import com.taskease.collegeapp.Adapter.TimeTableAdapter
import com.taskease.collegeapp.Constants.Constant
import com.taskease.collegeapp.Constants.CustomProgressDialog
import com.taskease.collegeapp.Constants.SharedPreferencesManager
import com.taskease.collegeapp.Model.Response.SpreadSheetResponseModel
import com.taskease.collegeapp.Model.Response.TimeTableResponseModel
import com.taskease.collegeapp.R
import com.taskease.collegeapp.Retrofit.RetrofitInstance
import com.taskease.collegeapp.databinding.ActivityGetAllExamScheduleBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GetAllExamScheduleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGetAllExamScheduleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGetAllExamScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.actionBar.title.text = "Exam Time Table"
        binding.actionBar.back.setOnClickListener{
            finish()
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
    }

    override fun onStart() {
        super.onStart()
        CallAllExamTimetables()
    }

    private fun CallAllExamTimetables() {
        val progressDialog = CustomProgressDialog(this@GetAllExamScheduleActivity)
        progressDialog.show()

        val token = SharedPreferencesManager.getString(this@GetAllExamScheduleActivity, SharedPreferencesManager.TOKEN)
        val yearId = SharedPreferencesManager.getInt(this@GetAllExamScheduleActivity, SharedPreferencesManager.YEAR_ID)
        val departmentId = SharedPreferencesManager.getInt(this@GetAllExamScheduleActivity, SharedPreferencesManager.DEPARTMENT_ID)
        val batchId = SharedPreferencesManager.getInt(this@GetAllExamScheduleActivity, SharedPreferencesManager.BATCH_ID)
        try {
            RetrofitInstance.getInstance().getTimeTableByAllDetails(token,yearId,departmentId,batchId,"EXAM")
                .enqueue(object : Callback<TimeTableResponseModel> {
                    override fun onResponse(
                        p0: Call<TimeTableResponseModel>,
                        p1: Response<TimeTableResponseModel>
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
                                        Constant.toast(this@GetAllExamScheduleActivity,"No Data Found")
                                        binding.noData.visibility = View.VISIBLE
                                    }
                                    else
                                    {
                                        binding.noData.visibility = View.GONE
                                        binding.recyclerView.adapter = TimeTableAdapter(this@GetAllExamScheduleActivity,data.CONTENT.toMutableList())
                                    }
                                }
                                else
                                {
                                    Constant.toast(this@GetAllExamScheduleActivity,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@GetAllExamScheduleActivity,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@GetAllExamScheduleActivity,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<TimeTableResponseModel>, p1: Throwable) {
                        Constant.toast(this@GetAllExamScheduleActivity,p1.message.toString())
                        progressDialog.dismiss()
                    }

                })
        }catch (e : Exception)
        {
            Constant.toast(this@GetAllExamScheduleActivity,"Something went wrong")
            e.printStackTrace()
            progressDialog.dismiss()
        }
    }
}