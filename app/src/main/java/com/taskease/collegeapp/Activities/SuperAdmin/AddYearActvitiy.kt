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
import com.taskease.collegeapp.Adapter.YearAdapter
import com.taskease.collegeapp.Constants.Constant
import com.taskease.collegeapp.Constants.CustomProgressDialog
import com.taskease.collegeapp.Constants.SharedPreferencesManager
import com.taskease.collegeapp.Model.Request.AddBannerRequestModel
import com.taskease.collegeapp.Model.Request.CreateYearRequestModel
import com.taskease.collegeapp.Model.Response.AddBannerResponseModel
import com.taskease.collegeapp.Model.Response.CreateYearResponseModel
import com.taskease.collegeapp.Model.Response.GetAllYearResponseModel
import com.taskease.collegeapp.R
import com.taskease.collegeapp.Retrofit.RetrofitInstance
import com.taskease.collegeapp.databinding.ActivityAddYearActvitiyBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddYearActvitiy : AppCompatActivity() {

    private lateinit var binding : ActivityAddYearActvitiyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddYearActvitiyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.actionBar.back.setOnClickListener{
            finish()
        }

        binding.actionBar.title.text = "Show Year"


        binding.recyclerView.layoutManager = LinearLayoutManager(this@AddYearActvitiy)

        binding.addYear.setOnClickListener{
            showCustomDialog()
        }

    }

    private fun showCustomDialog() {
        // Inflate custom layout
        val dialogView = LayoutInflater.from(this).inflate(R.layout.add_year_dialog, null)

        // Create and configure dialog
        val dialog = Dialog(this)
        dialog.setContentView(dialogView)

        val title = dialogView.findViewById<EditText>(R.id.title)
        val submit = dialogView.findViewById<Button>(R.id.submit)
        val close = dialogView.findViewById<Button>(R.id.close)

        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        submit.setOnClickListener {
            val title = title.text.toString()
            if (title.isEmpty()) {
                Constant.toast(this, "Please enter title")
            } else {
                CallAddYearApi(title , dialog)
            }
        }

        close.setOnClickListener{
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun CallAddYearApi(title: String , dialog: Dialog) {

        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()

        try {
            val token = SharedPreferencesManager.getString(this, SharedPreferencesManager.TOKEN)
            val model = CreateYearRequestModel(title)
            RetrofitInstance.getInstance().createYear(token,model)
                .enqueue(object : Callback<CreateYearResponseModel> {
                    override fun onResponse(
                        p0: Call<CreateYearResponseModel>,
                        p1: Response<CreateYearResponseModel>
                    ) {
                        if (p1.isSuccessful)
                        {
                            progressDialog.dismiss()
                            val data = p1.body()
                            if (data!=null)
                            {
                                if (data.STS == "200")
                                {
                                    Constant.toast(this@AddYearActvitiy,data.MSG)
                                    GetAllYears()
                                    dialog.dismiss()
                                }
                                else
                                {
                                    Constant.toast(this@AddYearActvitiy,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@AddYearActvitiy,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@AddYearActvitiy,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<CreateYearResponseModel>, p1: Throwable) {
                        Constant.toast(this@AddYearActvitiy,p1.message.toString())
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
        GetAllYears()
    }

    private fun GetAllYears() {
        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()

        try {
            RetrofitInstance.getInstance().getAllYears()
                .enqueue(object : Callback<GetAllYearResponseModel> {
                    override fun onResponse(
                        p0: Call<GetAllYearResponseModel>,
                        p1: Response<GetAllYearResponseModel>
                    ) {
                        if (p1.isSuccessful)
                        {
                            progressDialog.dismiss()
                            val data = p1.body()
                            if (data!=null)
                            {
                                if (data.STS == "200")
                                {
                                   binding.recyclerView.adapter = YearAdapter(this@AddYearActvitiy,data.CONTENT.toMutableList())
                                }
                                else
                                {
                                    Constant.toast(this@AddYearActvitiy,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@AddYearActvitiy,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@AddYearActvitiy,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<GetAllYearResponseModel>, p1: Throwable) {
                        Constant.toast(this@AddYearActvitiy,p1.message.toString())
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