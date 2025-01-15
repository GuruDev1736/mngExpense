package com.taskease.collegeapp.Activities.SuperAdmin

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputLayout
import com.taskease.collegeapp.Adapter.DepartmentAdapter
import com.taskease.collegeapp.Constants.Constant
import com.taskease.collegeapp.Constants.CustomProgressDialog
import com.taskease.collegeapp.Constants.SharedPreferencesManager
import com.taskease.collegeapp.Model.Request.CreateDepartmentRequestModel
import com.taskease.collegeapp.Model.Request.CreateYearRequestModel
import com.taskease.collegeapp.Model.Response.CreateDepartmentResponseModel
import com.taskease.collegeapp.Model.Response.CreateYearResponseModel
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

        binding.add.setOnClickListener{
            showCustomDialog()
        }
    }

    private fun showCustomDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.add_year_dialog, null)

        // Create and configure dialog
        val dialog = Dialog(this)
        dialog.setContentView(dialogView)

        val title = dialogView.findViewById<EditText>(R.id.title)
        val description = dialogView.findViewById<EditText>(R.id.description)
        val descriptionLayout = dialogView.findViewById<TextInputLayout>(R.id.descriptionLayout)
        val submit = dialogView.findViewById<Button>(R.id.submit)
        val close = dialogView.findViewById<Button>(R.id.close)

        descriptionLayout.visibility = View.VISIBLE

        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        submit.setOnClickListener {
            val title = title.text.toString()
            val description = description.text.toString()
            if (title.isEmpty() || description.isEmpty()) {
                Constant.toast(this, "Please Fill All the fields")
            } else {
                CallAddDepartment(title , description , dialog)
            }
        }

        close.setOnClickListener{
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun CallAddDepartment(title: String, description : String, dialog: Dialog) {

        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()

        try {
            val token = SharedPreferencesManager.getString(this, SharedPreferencesManager.TOKEN)
            val model = CreateDepartmentRequestModel(description,title)
            RetrofitInstance.getInstance().createDepartment(token,model)
                .enqueue(object : Callback<CreateDepartmentResponseModel> {
                    override fun onResponse(
                        p0: Call<CreateDepartmentResponseModel>,
                        p1: Response<CreateDepartmentResponseModel>
                    ) {
                        if (p1.isSuccessful)
                        {
                            progressDialog.dismiss()
                            val data = p1.body()
                            if (data!=null)
                            {
                                if (data.STS == "200")
                                {
                                    Constant.toast(this@AddDepartmentActivity,data.MSG)
                                    GetAllDepartments()
                                    dialog.dismiss()
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

                    override fun onFailure(p0: Call<CreateDepartmentResponseModel>, p1: Throwable) {
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
                                   binding.recyclerView.adapter = DepartmentAdapter(this@AddDepartmentActivity,data.CONTENT.toMutableList())
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