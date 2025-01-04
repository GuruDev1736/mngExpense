package com.taskease.collegeapp.Activities.Hostel

import android.app.Activity
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.taskease.collegeapp.Constants.Constant
import com.taskease.collegeapp.Constants.CustomProgressDialog
import com.taskease.collegeapp.Constants.SharedPreferencesManager
import com.taskease.collegeapp.Model.Request.CreateSpreadSheetRequestModel
import com.taskease.collegeapp.Model.Response.SingleSpreadSheetResponseModel
import com.taskease.collegeapp.R
import com.taskease.collegeapp.Retrofit.RetrofitInstance
import com.taskease.collegeapp.databinding.ActivityUploadPenaltyBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UploadPenaltyActivity : AppCompatActivity() {

    private lateinit var binding : ActivityUploadPenaltyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadPenaltyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.actionBar.back.setOnClickListener{
            finish()
        }

        binding.actionBar.title.text = "Upload Penalty"

        binding.submit.setOnClickListener{
            val title = binding.titleInput.text.toString()
            val description = binding.descriptionInput.text.toString()

            if (Valid(title,description))
            {
                CallCreatePenaltyApi(title,description)
            }
        }

    }

    private fun CallCreatePenaltyApi(title: String, description: String) {

        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()

        try {
            val token = SharedPreferencesManager.getString(this, SharedPreferencesManager.TOKEN)
            val userId = SharedPreferencesManager.getInt(this, SharedPreferencesManager.USERID)
            val model = CreateSpreadSheetRequestModel("PENALTY",description,"","",title)
            RetrofitInstance.getInstance().createPenalty(token,userId,model)
                .enqueue(object : Callback<SingleSpreadSheetResponseModel> {
                    override fun onResponse(
                        p0: Call<SingleSpreadSheetResponseModel>,
                        p1: Response<SingleSpreadSheetResponseModel>
                    ) {
                        if (p1.isSuccessful)
                        {
                            progressDialog.dismiss()
                            val data = p1.body()
                            if (data!=null)
                            {
                                if (data.STS == "200")
                                {
                                    Constant.toast(this@UploadPenaltyActivity,data.MSG)
                                    finish()
                                }
                                else
                                {
                                    Constant.toast(this@UploadPenaltyActivity,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@UploadPenaltyActivity,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@UploadPenaltyActivity,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<SingleSpreadSheetResponseModel>, p1: Throwable) {
                        Constant.toast(this@UploadPenaltyActivity,p1.message.toString())
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
            return false
        }

        if (description.isEmpty())
        {
            binding.descriptionInputLayout.error = "Description is required"
            return false
        }

        return true
    }
}