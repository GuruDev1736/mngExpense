package com.taskease.collegeapp.Auth

import android.content.Intent
import android.os.Bundle
import android.provider.SyncStateContract.Constants
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.taskease.collegeapp.Activities.HomeActivity
import com.taskease.collegeapp.Constants.Constant
import com.taskease.collegeapp.Constants.CustomProgressDialog
import com.taskease.collegeapp.Constants.SharedPreferencesManager
import com.taskease.collegeapp.Model.Request.LoginRequestModel
import com.taskease.collegeapp.Model.Response.LoginResponseModel
import com.taskease.collegeapp.R
import com.taskease.collegeapp.Retrofit.RetrofitInstance
import com.taskease.collegeapp.databinding.ActivityLoginBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var binding : ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.etEmail.setText(SharedPreferencesManager.getString(this,SharedPreferencesManager.EMAIL))
        binding.etPassword.setText(SharedPreferencesManager.getString(this,SharedPreferencesManager.PASSWORD))

        binding.btnLogin.setOnClickListener{
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (valid(email,password))
            {
                callLoginApi(email,password)
            }
        }

    }

    private fun callLoginApi(email: String, password: String) {
        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()

        try {
            val model = LoginRequestModel(email,password)
            RetrofitInstance.getInstance().login(model).enqueue(object : Callback<LoginResponseModel>{
                override fun onResponse(
                    p0: Call<LoginResponseModel>,
                    p1: Response<LoginResponseModel>
                ) {
                    if (p1.isSuccessful)
                    {
                        progressDialog.dismiss()
                        val data = p1.body()
                        if (data!=null)
                        {
                            if (data.STS == "200")
                            {
                                SharedPreferencesManager.saveString(this@LoginActivity,SharedPreferencesManager.EMAIL,email)
                                SharedPreferencesManager.saveString(this@LoginActivity,SharedPreferencesManager.PASSWORD,password)
                                SharedPreferencesManager.saveString(this@LoginActivity,SharedPreferencesManager.TOKEN,"Bearer "+data.CONTENT.token)
                                SharedPreferencesManager.saveString(this@LoginActivity,SharedPreferencesManager.USERNAME,data.CONTENT.userName)
                                SharedPreferencesManager.saveString(this@LoginActivity,SharedPreferencesManager.ROLE,data.CONTENT.userRole)
                                SharedPreferencesManager.saveInt(this@LoginActivity,SharedPreferencesManager.USERID,data.CONTENT.userId)
                                SharedPreferencesManager.saveInt(this@LoginActivity,SharedPreferencesManager.DEPARTMENTID,data.CONTENT.departmentId)

                                Constant.toast(this@LoginActivity,data.MSG)
                                startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                                finish()
                            }
                            else
                            {
                                Constant.toast(this@LoginActivity,data.MSG)
                            }
                        }
                    }
                    else{
                        Constant.toast(this@LoginActivity,p1.message().toString())
                        progressDialog.dismiss()
                    }
                }

                override fun onFailure(p0: Call<LoginResponseModel>, p1: Throwable) {
                    Constant.toast(this@LoginActivity,p1.message.toString())
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

    private fun valid(email: String, password: String): Boolean {
       if (email.isEmpty())
       {
           binding.etEmail.setError("Enter Email")
           binding.etEmail.requestFocus()
           return false
       }
        if (password.isEmpty()) {
            binding.etPassword.setError("Enter Password")
            binding.etPassword.requestFocus()
            return false
        }
        return true
    }
}