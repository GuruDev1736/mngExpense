package com.taskease.collegeapp.Activities.SuperAdmin

import android.content.Intent
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
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.taskease.collegeapp.Activities.Faculty.Attendance.AddAttendanceActivity
import com.taskease.collegeapp.Activities.Faculty.Attendance.MyFragmentAdapter
import com.taskease.collegeapp.Adapter.UserAdapter
import com.taskease.collegeapp.Constants.Constant
import com.taskease.collegeapp.Constants.CustomProgressDialog
import com.taskease.collegeapp.Constants.SharedPreferencesManager
import com.taskease.collegeapp.Model.Response.GetAllDepartmentResponseModel
import com.taskease.collegeapp.Model.Response.GetAllRoleResponseModel
import com.taskease.collegeapp.Model.Response.GetAllUsersResponseModel
import com.taskease.collegeapp.R
import com.taskease.collegeapp.Retrofit.RetrofitInstance
import com.taskease.collegeapp.databinding.ActivityShowUsersBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ShowUsersActivity : AppCompatActivity() {

    private lateinit var binding : ActivityShowUsersBinding
    private var selectedRole = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.actionBar.back.setOnClickListener{
            finish()
        }
        binding.actionBar.title.text = "Users"
        binding.createuser.setOnClickListener{
            startActivity(Intent(this, CreateUserActivity::class.java))
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)

    }

    override fun onStart() {
        super.onStart()
        GetAllRoles()
    }

    private fun GetAllRoles() {
        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()

        val token = SharedPreferencesManager.getString(this,SharedPreferencesManager.TOKEN)

        try {
            RetrofitInstance.getInstance().getAllRoles(token)
                .enqueue(object : Callback<GetAllRoleResponseModel> {
                    override fun onResponse(
                        p0: Call<GetAllRoleResponseModel>,
                        p1: Response<GetAllRoleResponseModel>
                    ) {
                        if (p1.isSuccessful)
                        {
                            progressDialog.dismiss()
                            val data = p1.body()
                            if (data!=null)
                            {
                                if (data.STS == "200")
                                {
                                    val departmentList = data.CONTENT.filter { it.roleName != "ROLE_STUDENT" }
                                    val departmentNames = departmentList.map { it.roleName } // Extract names

                                    // Create an ArrayAdapter for the Spinner
                                    val adapter = ArrayAdapter(
                                        this@ShowUsersActivity,
                                        android.R.layout.simple_spinner_item,
                                        departmentNames
                                    )
                                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                                    binding.roles.adapter = adapter

                                    // Set listener to get the selected department's ID
                                    binding.roles.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                        override fun onItemSelected(
                                            parentView: AdapterView<*>,
                                            selectedItemView: View?,
                                            position: Int,
                                            id: Long
                                        ) {
                                            selectedRole= departmentList[position].roleName
                                            GetAllUsersAccordingTRole(selectedRole)
                                        }

                                        override fun onNothingSelected(parentView: AdapterView<*>) {

                                        }
                                    }
                                }
                                else
                                {
                                    Constant.toast(this@ShowUsersActivity,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@ShowUsersActivity,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@ShowUsersActivity,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<GetAllRoleResponseModel>, p1: Throwable) {
                        Constant.toast(this@ShowUsersActivity,p1.message.toString())
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

    private fun GetAllUsersAccordingTRole(selectedRole: String) {
        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()

        val token = SharedPreferencesManager.getString(this,SharedPreferencesManager.TOKEN)

        try {
            RetrofitInstance.getInstance().getAllUsersByRole(token,selectedRole)
                .enqueue(object : Callback<GetAllUsersResponseModel> {
                    override fun onResponse(
                        p0: Call<GetAllUsersResponseModel>,
                        p1: Response<GetAllUsersResponseModel>
                    ) {
                        if (p1.isSuccessful)
                        {
                            progressDialog.dismiss()
                            val data = p1.body()
                            if (data!=null)
                            {
                                if (data.STS == "200")
                                {
                                    binding.recyclerView.adapter = UserAdapter(this@ShowUsersActivity,data.CONTENT)
                                }
                                else
                                {
                                    Constant.toast(this@ShowUsersActivity,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@ShowUsersActivity,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@ShowUsersActivity,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<GetAllUsersResponseModel>, p1: Throwable) {
                        Constant.toast(this@ShowUsersActivity,p1.message.toString())
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