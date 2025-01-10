package com.taskease.collegeapp.Activities.SuperAdmin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.taskease.collegeapp.Adapter.StoreAdapter
import com.taskease.collegeapp.Constants.Constant
import com.taskease.collegeapp.Constants.CustomProgressDialog
import com.taskease.collegeapp.Constants.SharedPreferencesManager
import com.taskease.collegeapp.Model.Response.GetAllRoleResponseModel
import com.taskease.collegeapp.Model.Response.GetAllStoreResponseModel
import com.taskease.collegeapp.R
import com.taskease.collegeapp.Retrofit.RetrofitInstance
import com.taskease.collegeapp.databinding.ActivityShowAllStoreBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ShowAllStoreActivity : AppCompatActivity() {

    private lateinit var binding : ActivityShowAllStoreBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowAllStoreBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.actionBar.title.text = "All Stores"
        binding.actionBar.back.setOnClickListener{
            finish()
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        binding.createStore.setOnClickListener{
            startActivity(Intent(this@ShowAllStoreActivity,CreateStoreActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        GetAllStores()
    }

    private fun GetAllStores() {
        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()

        val token = SharedPreferencesManager.getString(this, SharedPreferencesManager.TOKEN)

        try {
            RetrofitInstance.getInstance().getAllStore(token)
                .enqueue(object : Callback<GetAllStoreResponseModel> {
                    override fun onResponse(
                        p0: Call<GetAllStoreResponseModel>,
                        p1: Response<GetAllStoreResponseModel>
                    ) {
                        if (p1.isSuccessful)
                        {
                            progressDialog.dismiss()
                            val data = p1.body()
                            if (data!=null)
                            {
                                if (data.STS == "200")
                                {
                                    binding.recyclerView.adapter = StoreAdapter(this@ShowAllStoreActivity,data.CONTENT.toMutableList())
                                }
                                else
                                {
                                    Constant.toast(this@ShowAllStoreActivity,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@ShowAllStoreActivity,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@ShowAllStoreActivity,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<GetAllStoreResponseModel>, p1: Throwable) {
                        Constant.toast(this@ShowAllStoreActivity,p1.message.toString())
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