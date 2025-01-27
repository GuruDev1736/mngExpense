package com.taskease.collegeapp.Activities.Student.Coopertative

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import com.taskease.collegeapp.Activities.Lib.ShowBookActivity
import com.taskease.collegeapp.Adapter.DashboardAdapter
import com.taskease.collegeapp.Adapter.StocksAdapter
import com.taskease.collegeapp.Constants.Constant
import com.taskease.collegeapp.Constants.CustomProgressDialog
import com.taskease.collegeapp.Constants.SharedPreferencesManager
import com.taskease.collegeapp.Model.DashboardItem
import com.taskease.collegeapp.Model.Response.GetAllStocksResponseModel
import com.taskease.collegeapp.Model.Response.UniversalResponseModel
import com.taskease.collegeapp.R
import com.taskease.collegeapp.Retrofit.RetrofitInstance
import com.taskease.collegeapp.databinding.ActivityStockBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StockActivity : AppCompatActivity() {

    private lateinit var binding : ActivityStockBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStockBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.actionBar.title.text = "Stocks"
        binding.actionBar.back.setOnClickListener{
            finish()
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this@StockActivity)


    }

    override fun onStart() {
        super.onStart()
        getStatusApi("GODOWN")
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
                                        Constant.toast(this@StockActivity,"No Data Found")
                                        binding.noData.visibility = View.VISIBLE
                                    }
                                    else
                                    {
                                        binding.noData.visibility = View.GONE
                                    }
                                    val adapter = StocksAdapter(this@StockActivity,data.CONTENT.toMutableList())
                                    binding.recyclerView.adapter = adapter
                                }
                                else
                                {
                                    Constant.toast(this@StockActivity,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@StockActivity,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@StockActivity,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<GetAllStocksResponseModel>, p1: Throwable) {
                        Constant.toast(this@StockActivity,p1.message.toString())
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


    private fun getStatusApi(category: String) {
        val progressDialog = CustomProgressDialog(this@StockActivity)
        progressDialog.show()

        try {
            val token = SharedPreferencesManager.getString(this@StockActivity, SharedPreferencesManager.TOKEN)

            RetrofitInstance.getInstance().getStatus(token,category).enqueue(object :
                Callback<UniversalResponseModel> {
                override fun onResponse(
                    p0: Call<UniversalResponseModel>,
                    p1: Response<UniversalResponseModel>
                ) {
                    if (p1.isSuccessful) {
                        progressDialog.dismiss()
                        val data = p1.body()
                        if (data != null && data.STS == "200") {
                            if (data.CONTENT.toBoolean())
                            {
                                binding.status.text = "Godown is Open"
                                binding.status.setTextColor(Color.GREEN)
                            }
                            else
                            {
                                binding.status.text = "Godown is Close"
                                binding.status.setTextColor(Color.RED)
                            }
                        } else {
                            Constant.toast(this@StockActivity, data?.MSG ?: "Error")
                        }
                    } else {
                        Constant.toast(this@StockActivity, p1.message().toString())
                        progressDialog.dismiss()
                    }
                }

                override fun onFailure(p0: Call<UniversalResponseModel>, p1: Throwable) {
                    Constant.toast(this@StockActivity, p1.message.toString())
                    progressDialog.dismiss()
                }
            })
        }catch (e : Exception)
        {
            Constant.toast(this@StockActivity,"Something went wrong")
            e.printStackTrace()
            progressDialog.dismiss()
        }
    }
}