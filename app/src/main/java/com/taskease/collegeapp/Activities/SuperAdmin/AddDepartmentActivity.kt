package com.taskease.collegeapp.Activities.SuperAdmin

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
import com.taskease.collegeapp.Adapter.DepartmentAdapter
import com.taskease.collegeapp.Constants.Constant
import com.taskease.collegeapp.Constants.CustomProgressDialog
import com.taskease.collegeapp.Model.Response.GetAllDepartmentResponseModel
import com.taskease.collegeapp.R
import com.taskease.collegeapp.Retrofit.RetrofitInstance
import com.taskease.collegeapp.databinding.ActivityAddDepartmentBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddDepartmentActivity : AppCompatActivity() {

    private lateinit var binding : ActivityAddDepartmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddDepartmentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.actionBar.title.text = "Departments"
        binding.actionBar.back.setOnClickListener{
            finish()
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this@AddDepartmentActivity)



    }

    override fun onStart() {
        super.onStart()
        GetAllDepartments()
    }


    private fun GetAllDepartments() {
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
                                   binding.recyclerView.adapter = DepartmentAdapter(this@AddDepartmentActivity,data.CONTENT)
                                }
                                else
                                {
                                    Constant.toast(this@AddDepartmentActivity,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@AddDepartmentActivity,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@AddDepartmentActivity,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<GetAllDepartmentResponseModel>, p1: Throwable) {
                        Constant.toast(this@AddDepartmentActivity,p1.message.toString())
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