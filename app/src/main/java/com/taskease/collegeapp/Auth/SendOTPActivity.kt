package com.taskease.collegeapp.Auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.taskease.collegeapp.Constants.Constant
import com.taskease.collegeapp.Constants.CustomProgressDialog
import com.taskease.collegeapp.Model.Response.UniversalResponseModel
import com.taskease.collegeapp.R
import com.taskease.collegeapp.Retrofit.RetrofitInstance
import com.taskease.collegeapp.databinding.ActivitySendOtpactivityBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SendOTPActivity : AppCompatActivity() {

    private lateinit var binding : ActivitySendOtpactivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySendOtpactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.actionBar.title.text = "Forgot Password"
        binding.actionBar.back.setOnClickListener {
            finish()
        }

        binding.conti.setOnClickListener{
            val email = binding.email.text.toString()

            if (email.isEmpty())
            {
                binding.email.error = "Email is required"
                return@setOnClickListener
            }
            sendOTPFunc(email)
        }
    }

    private fun sendOTPFunc(email: String) {

        val progress = CustomProgressDialog(this)
        progress.show()

        try {
            RetrofitInstance.getInstance().sendOTP(email).enqueue(object :
                Callback<UniversalResponseModel> {
                override fun onResponse(
                    p0: Call<UniversalResponseModel>,
                    p1: Response<UniversalResponseModel>
                ) {
                    if (p1.isSuccessful) {
                        progress.dismiss()
                        val response = p1.body()
                        if (response != null) {
                            if (response.STS == "200") {
                                Constant.toast(this@SendOTPActivity, response.MSG)
                                startActivity(
                                    Intent(this@SendOTPActivity, ValidateOTPActivity::class.java)
                                    .putExtra("email",email)
                                )
                            }
                            else
                            {
                                Constant.toast(this@SendOTPActivity , response.MSG)
                            }
                        }
                    }
                    else
                    {
                        progress.dismiss()
                        Constant.toast(this@SendOTPActivity , "Something went wrong")
                    }
                }

                override fun onFailure(p0: Call<UniversalResponseModel>, p1: Throwable) {
                    progress.dismiss()
                    Constant.toast(this@SendOTPActivity , "Something went wrong")
                }
            })
        }catch (e : Exception)
        {
            e.printStackTrace()
        }
    }
}