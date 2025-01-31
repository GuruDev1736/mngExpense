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
import com.taskease.collegeapp.databinding.ActivityChangePasswordBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class changePasswordActivity : AppCompatActivity() {

    private lateinit var binding : ActivityChangePasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.actionBar.title.text = "Change Password"
        binding.actionBar.back.setOnClickListener {
            finish()
        }

        val email = intent.getStringExtra("email").toString()

        binding.change.setOnClickListener{
            val password = binding.password.text.toString()
            if (password.isEmpty())
            {
                binding.password.error = "Password is required"
                return@setOnClickListener
            }
            else
            {
                changePasswordFunc(email,password)
            }
        }

    }

    private fun changePasswordFunc(email: String, password: String) {
        val progress = CustomProgressDialog(this)
        progress.show()
        try {
            RetrofitInstance.getInstance().changePassword(email,password).enqueue(object :
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
                                Constant.toast(this@changePasswordActivity, response.MSG)
                                val intent = Intent(this@changePasswordActivity,LoginActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                            }
                            else
                            {
                                Constant.toast(this@changePasswordActivity , response.MSG)
                            }
                        }
                    }
                    else
                    {
                        progress.dismiss()
                        Constant.toast(this@changePasswordActivity , "Something went wrong")
                    }
                }

                override fun onFailure(p0: Call<UniversalResponseModel>, p1: Throwable) {
                    progress.dismiss()
                    Constant.toast(this@changePasswordActivity , "Something went wrong")
                }
            })
        }catch (e : Exception)
        {
            e.printStackTrace()
        }
    }
}