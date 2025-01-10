package com.taskease.collegeapp.Activities.SuperAdmin

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.taskease.collegeapp.Constants.Constant
import com.taskease.collegeapp.Constants.CustomProgressDialog
import com.taskease.collegeapp.Constants.SharedPreferencesManager
import com.taskease.collegeapp.Model.Request.CreateStoreRequestModel
import com.taskease.collegeapp.Model.Response.CreateStoreResponseModel
import com.taskease.collegeapp.Model.Response.UserUniversalResponseModel
import com.taskease.collegeapp.R
import com.taskease.collegeapp.Retrofit.RetrofitInstance
import com.taskease.collegeapp.databinding.ActivityCreateStoreBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreateStoreActivity : AppCompatActivity() {

    private lateinit var binding : ActivityCreateStoreBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateStoreBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.actionBar.back.setOnClickListener{
            finish()
        }

        binding.actionBar.title.text = "Create Store"

        val adapter = ArrayAdapter.createFromResource(this@CreateStoreActivity,R.array.build_category,android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.categories.adapter = adapter

        binding.submit.setOnClickListener{

            val category = binding.categories.selectedItem.toString()

            callCreateStore(category)
        }
    }

    private fun callCreateStore(category: String) {

        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()

        val model = CreateStoreRequestModel(category)
        val token = SharedPreferencesManager.getString(this, SharedPreferencesManager.TOKEN)

        try {
            RetrofitInstance.getInstance().createStore(token,model)
                .enqueue(object : Callback<CreateStoreResponseModel> {
                    override fun onResponse(
                        p0: Call<CreateStoreResponseModel>,
                        p1: Response<CreateStoreResponseModel>
                    ) {
                        if (p1.isSuccessful)
                        {
                            progressDialog.dismiss()
                            val data = p1.body()
                            if (data!=null)
                            {
                                if (data.STS == "200")
                                {
                                    Constant.toast(this@CreateStoreActivity,data.MSG)
                                    finish()
                                }
                                else
                                {
                                    Constant.toast(this@CreateStoreActivity,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@CreateStoreActivity,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@CreateStoreActivity,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<CreateStoreResponseModel>, p1: Throwable) {
                        Constant.toast(this@CreateStoreActivity,p1.message.toString())
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