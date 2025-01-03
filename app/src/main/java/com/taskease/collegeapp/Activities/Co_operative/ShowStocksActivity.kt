package com.taskease.collegeapp.Activities.Co_operative

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.taskease.collegeapp.Activities.Lib.AddBooksActivity
import com.taskease.collegeapp.Adapter.BooksAdapter
import com.taskease.collegeapp.Adapter.StocksAdapter
import com.taskease.collegeapp.Constants.Constant
import com.taskease.collegeapp.Constants.CustomProgressDialog
import com.taskease.collegeapp.Constants.SharedPreferencesManager
import com.taskease.collegeapp.Model.Response.GetAllBooksResponseModel
import com.taskease.collegeapp.Model.Response.GetAllStocksResponseModel
import com.taskease.collegeapp.R
import com.taskease.collegeapp.Retrofit.RetrofitInstance
import com.taskease.collegeapp.databinding.ActivityShowStocksBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ShowStocksActivity : AppCompatActivity() {

    private lateinit var binding : ActivityShowStocksBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowStocksBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.actionBar.back.setOnClickListener{
            finish()
        }
        binding.actionBar.title.text = "Stocks"

        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        binding.addSpreadSheet.setOnClickListener{
            startActivity(Intent(this, AddStockActivity::class.java))
        }
    }


    override fun onStart() {
        super.onStart()
        CallGetAllStocks()
    }


    private fun CallGetAllStocks() {
        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()

        try {
            val token = SharedPreferencesManager.getString(this, SharedPreferencesManager.TOKEN)

            RetrofitInstance.getInstance().getAllStocks(token)
                .enqueue(object : Callback<GetAllStocksResponseModel> {
                    override fun onResponse(
                        p0: Call<GetAllStocksResponseModel>,
                        p1: Response<GetAllStocksResponseModel>
                    ) {
                        if (p1.isSuccessful)
                        {
                            progressDialog.dismiss()
                            val data = p1.body()
                            if (data!=null)
                            {
                                if (data.STS == "200")
                                {
                                    if (data.CONTENT.isEmpty())
                                    {
                                        Constant.toast(this@ShowStocksActivity,"No Data Found")
                                        binding.noData.visibility = View.VISIBLE
                                    }
                                    else
                                    {
                                        binding.noData.visibility = View.GONE
                                    }
                                    val adapter = StocksAdapter(this@ShowStocksActivity,data.CONTENT.toMutableList())
                                    binding.recyclerView.adapter = adapter
                                }
                                else
                                {
                                    Constant.toast(this@ShowStocksActivity,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@ShowStocksActivity,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@ShowStocksActivity,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<GetAllStocksResponseModel>, p1: Throwable) {
                        Constant.toast(this@ShowStocksActivity,p1.message.toString())
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