package com.taskease.collegeapp.Activities.Principle.WorkLoad

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.taskease.collegeapp.Adapter.SpreadSheetAdapter
import com.taskease.collegeapp.Constants.Constant
import com.taskease.collegeapp.Constants.CustomProgressDialog
import com.taskease.collegeapp.Constants.SharedPreferencesManager
import com.taskease.collegeapp.Model.Response.GetAllDepartmentResponseModel
import com.taskease.collegeapp.Model.Response.SpreadSheetResponseModel
import com.taskease.collegeapp.R
import com.taskease.collegeapp.Retrofit.RetrofitInstance
import com.taskease.collegeapp.databinding.ActivityShowWorkLoadReportBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ShowWorkLoadReportActivity : AppCompatActivity() {

    private lateinit var binding : ActivityShowWorkLoadReportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowWorkLoadReportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.actionBar.back.setOnClickListener{
            finish()
        }
        binding.actionBar.title.text = "WorkLoad Reports"

        binding.recyclerView.layoutManager = LinearLayoutManager(this)


    }


    override fun onStart() {
        super.onStart()
        callDepartments()
    }

    private fun callDepartments() {
        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()

        try {
            RetrofitInstance.getInstance().getAllDepartments()
                .enqueue(object : Callback<GetAllDepartmentResponseModel> {
                    override fun onResponse(
                        p0: Call<GetAllDepartmentResponseModel>,
                        p1: Response<GetAllDepartmentResponseModel>
                    ) {
                        if (p1.isSuccessful)
                        {
                            progressDialog.dismiss()
                            val data = p1.body()
                            if (data!=null)
                            {
                                if (data.STS == "200")
                                {
                                    val departmentList = data.CONTENT
                                    val departmentNames = departmentList.map { it.name } // Extract names

                                    // Create an ArrayAdapter for the Spinner
                                    val adapter = ArrayAdapter<String>(
                                        this@ShowWorkLoadReportActivity,
                                        android.R.layout.simple_spinner_item,
                                        departmentNames
                                    )
                                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                                    binding.departments.adapter = adapter

                                    // Set listener to get the selected department's ID
                                    binding.departments.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                        override fun onItemSelected(
                                            parentView: AdapterView<*>,
                                            selectedItemView: View?,
                                            position: Int,
                                            id: Long
                                        ) {
                                            val selectedDepartmentId = departmentList[position].id
                                            Log.d(
                                                "Selected Department ID",
                                                selectedDepartmentId.toString()
                                            )

                                            CallGetAllSpreadSheet(selectedDepartmentId)
                                        }

                                        override fun onNothingSelected(parentView: AdapterView<*>) {

                                        }
                                    }
                                }
                                else
                                {
                                    Constant.toast(this@ShowWorkLoadReportActivity,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@ShowWorkLoadReportActivity,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@ShowWorkLoadReportActivity,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<GetAllDepartmentResponseModel>, p1: Throwable) {
                        Constant.toast(this@ShowWorkLoadReportActivity,p1.message.toString())
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

    private fun CallGetAllSpreadSheet(department : Int) {
        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()

        try {
            val token = SharedPreferencesManager.getString(this, SharedPreferencesManager.TOKEN)

            RetrofitInstance.getInstance().getAllSpreadsheetByCategory(token,"WORKLOAD",department)
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
                                        Constant.toast(this@ShowWorkLoadReportActivity,"No Data Found")
                                        binding.noData.visibility = View.VISIBLE
                                    }
                                    else
                                    {
                                        binding.noData.visibility = View.GONE
                                    }
                                    val adapter = SpreadSheetAdapter(this@ShowWorkLoadReportActivity,data.CONTENT.toMutableList(),"WORKLOAD_P")
                                    binding.recyclerView.adapter = adapter
                                }
                                else
                                {
                                    Constant.toast(this@ShowWorkLoadReportActivity,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@ShowWorkLoadReportActivity,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@ShowWorkLoadReportActivity,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<SpreadSheetResponseModel>, p1: Throwable) {
                        Constant.toast(this@ShowWorkLoadReportActivity,p1.message.toString())
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