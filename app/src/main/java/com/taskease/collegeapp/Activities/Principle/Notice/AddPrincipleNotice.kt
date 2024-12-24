package com.taskease.collegeapp.Activities.Principle.Notice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.taskease.collegeapp.Constants.Constant
import com.taskease.collegeapp.Constants.CustomProgressDialog
import com.taskease.collegeapp.Constants.SharedPreferencesManager
import com.taskease.collegeapp.Model.Request.CreateSpreadSheetRequestModel
import com.taskease.collegeapp.Model.Response.SingleSpreadSheetResponseModel
import com.taskease.collegeapp.Model.Response.UniversalResponseModel
import com.taskease.collegeapp.R
import com.taskease.collegeapp.Retrofit.RetrofitInstance
import com.taskease.collegeapp.databinding.ActivityShowNoticesBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddPrincipleNotice : AppCompatActivity() {

    private lateinit var binding : ActivityShowNoticesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowNoticesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.actionBar.title.text = "Notice"
        binding.actionBar.back.setOnClickListener{
            finish()
        }

        binding.category.visibility = View.VISIBLE
            val adapter = ArrayAdapter.createFromResource(this@AddPrincipleNotice,R.array.NoticeCategory,android.R.layout.simple_spinner_item)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.category.adapter = adapter

            binding.submit.setOnClickListener{
                val title = binding.titleInput.text.toString()
                val description = binding.descriptionInput.text.toString()
                val category = binding.category.selectedItem.toString()

                if (Valid(title,description))
                {
                    if (category == "FACULTY")
                    {
                        CallNoticeApi("For Faculty : $title",description)
                    }
                    else if (category == "STUDENT")
                    {
                        CallNoticeApi("For Student : $title",description)
                    }
                    else if (category == "EVENT")
                    {
                        CallNoticeApi("For Event : $title",description)
                    }
                }
            }
    }

    private fun CallNoticeApi(title: String, description: String) {

        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()

        try {
            val token = SharedPreferencesManager.getString(this, SharedPreferencesManager.TOKEN)
            RetrofitInstance.getInstance().sendNotification(token,title,description)
                .enqueue(object : Callback<UniversalResponseModel> {
                    override fun onResponse(
                        p0: Call<UniversalResponseModel>,
                        p1: Response<UniversalResponseModel>
                    ) {
                        if (p1.isSuccessful)
                        {
                            progressDialog.dismiss()
                            val data = p1.body()
                            if (data!=null)
                            {
                                if (data.STS == "200")
                                {
                                    Constant.toast(this@AddPrincipleNotice,data.MSG)
                                    finish()
                                }
                                else
                                {
                                    Constant.toast(this@AddPrincipleNotice,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@AddPrincipleNotice,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@AddPrincipleNotice,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<UniversalResponseModel>, p1: Throwable) {
                        Constant.toast(this@AddPrincipleNotice,p1.message.toString())
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


    private fun Valid(title: String, description: String): Boolean {

        if (title.isEmpty())
        {
            binding.titleInputLayout.error = "Title is required"
            binding.titleInput.requestFocus()
            return false
        }
        if (description.isEmpty())
        {
            binding.descriptionInputLayout.error = "Description is required"
            binding.descriptionInput.requestFocus()
            return false
        }
        return true
    }
}