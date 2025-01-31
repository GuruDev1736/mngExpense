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
import com.taskease.collegeapp.databinding.ActivityValidateOtpactivityBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ValidateOTPActivity : AppCompatActivity() {

    private lateinit var binding : ActivityValidateOtpactivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityValidateOtpactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.actionBar.title.text = "OTP Verification"
        binding.actionBar.back.setOnClickListener {
            finish()
        }

        val email = intent.getStringExtra("email").toString()

        binding.verify.setOnClickListener{
            val otp = binding.otpView.otp.toString();

            if(otp.isEmpty())
            {
                binding.otpView.showError()
                Constant.toast(this@ValidateOTPActivity,"Please enter the OTP")
                return@setOnClickListener
            }
            else
            {
                ValidateOTPFunc(otp,email)
            }
        }
    }

    private fun ValidateOTPFunc(otp: String, email: String) {
        val progress = CustomProgressDialog(this)
        progress.show()
        try {
            RetrofitInstance.getInstance().validateOTP(email,otp).enqueue(object :
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
                                Constant.toast(this@ValidateOTPActivity, response.MSG)
                                startActivity(
                                    Intent(this@ValidateOTPActivity, changePasswordActivity::class.java)
                                        .putExtra("email",email)
                                )
                            }
                            else
                            {
                                Constant.toast(this@ValidateOTPActivity , response.MSG)
                            }
                        }
                    }
                    else
                    {
                        progress.dismiss()
                        Constant.toast(this@ValidateOTPActivity , "Something went wrong")
                    }
                }

                override fun onFailure(p0: Call<UniversalResponseModel>, p1: Throwable) {
                    progress.dismiss()
                    Constant.toast(this@ValidateOTPActivity , "Something went wrong")
                }
            })
        }catch (e : Exception)
        {
            e.printStackTrace()
        }
    }
}