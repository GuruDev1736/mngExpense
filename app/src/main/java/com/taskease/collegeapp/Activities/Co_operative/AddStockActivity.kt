package com.taskease.collegeapp.Activities.Co_operative

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.taskease.collegeapp.Constants.Constant
import com.taskease.collegeapp.Constants.CustomProgressDialog
import com.taskease.collegeapp.Constants.SharedPreferencesManager
import com.taskease.collegeapp.Model.Request.AddBookRequestModel
import com.taskease.collegeapp.Model.Request.AddStockRequestModel
import com.taskease.collegeapp.Model.Response.AddBookResponseModel
import com.taskease.collegeapp.Model.Response.AddStockResponseModel
import com.taskease.collegeapp.R
import com.taskease.collegeapp.Retrofit.RetrofitInstance
import com.taskease.collegeapp.databinding.ActivityAddStockBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddStockActivity : AppCompatActivity() {

    private lateinit var binding : ActivityAddStockBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStockBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.actionBar.back.setOnClickListener{
            finish()
        }

        binding.actionBar.title.text = "Add Stock"


        binding.submit.setOnClickListener{

            val title = binding.Stocktitle.text.toString()
            val description = binding.description.text.toString()
            val price = binding.price.text.toString()

            if (Valid(title,description,price))
            {
                CallCreateStockApi(title,description,price)
            }
        }
    }

    private fun CallCreateStockApi(title: String, description: String, price: String) {

        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()

        try {
            val token = SharedPreferencesManager.getString(this, SharedPreferencesManager.TOKEN)
            val model = AddStockRequestModel(description,price,title)
            RetrofitInstance.getInstance().addStock(token,model)
                .enqueue(object : Callback<AddStockResponseModel> {
                    override fun onResponse(
                        p0: Call<AddStockResponseModel>,
                        p1: Response<AddStockResponseModel>
                    ) {
                        if (p1.isSuccessful)
                        {
                            progressDialog.dismiss()
                            val data = p1.body()
                            if (data!=null)
                            {
                                if (data.STS == "200")
                                {
                                    Constant.toast(this@AddStockActivity,data.MSG)
                                    finish()
                                }
                                else
                                {
                                    Constant.toast(this@AddStockActivity,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@AddStockActivity,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@AddStockActivity,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<AddStockResponseModel>, p1: Throwable) {
                        Constant.toast(this@AddStockActivity,p1.message.toString())
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

    private fun Valid(title: String, description: String, price : String): Boolean {

        if (title.isEmpty())
        {
            binding.Stocktitle.error = "Title is required"
            binding.Stocktitle.requestFocus()
            return false
        }
        else if (description.isEmpty()) {
            binding.description.error = "Description is required"
            binding.description.requestFocus()
            return false
        }
        else if (price.isEmpty()) {
            binding.price.error = "Price is required"
            binding.price.requestFocus()
            return false
        }
        return true
    }
}