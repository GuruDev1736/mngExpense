package com.taskease.collegeapp.Activities.Lib

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.taskease.collegeapp.Activities.Faculty.SpreadSheet.SpreadSheetActivity
import com.taskease.collegeapp.Adapter.BooksAdapter
import com.taskease.collegeapp.Adapter.SpreadSheetAdapter
import com.taskease.collegeapp.Constants.Constant
import com.taskease.collegeapp.Constants.CustomProgressDialog
import com.taskease.collegeapp.Constants.SharedPreferencesManager
import com.taskease.collegeapp.Model.Response.GetAllBooksResponseModel
import com.taskease.collegeapp.Model.Response.SpreadSheetResponseModel
import com.taskease.collegeapp.R
import com.taskease.collegeapp.Retrofit.RetrofitInstance
import com.taskease.collegeapp.databinding.ActivityAddBooksBinding
import com.taskease.collegeapp.databinding.ActivityShowBookBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ShowBookActivity : AppCompatActivity() {

    private lateinit var  binding: ActivityShowBookBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowBookBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.actionBar.back.setOnClickListener{
            finish()
        }
        binding.actionBar.title.text = "Books"

        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        val role = SharedPreferencesManager.getString(this, SharedPreferencesManager.ROLE)
        if(role.equals("ROLE_STUDENT"))
        {
            binding.addSpreadSheet.visibility = View.GONE
        }

        binding.addSpreadSheet.setOnClickListener{
            startActivity(Intent(this, AddBooksActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()

        CallGetAllBooks()
    }

    private fun CallGetAllBooks() {
        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()

        try {
            val token = SharedPreferencesManager.getString(this, SharedPreferencesManager.TOKEN)

            RetrofitInstance.getInstance().getAllBooks(token)
                .enqueue(object : Callback<GetAllBooksResponseModel> {
                    override fun onResponse(
                        p0: Call<GetAllBooksResponseModel>,
                        p1: Response<GetAllBooksResponseModel>
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
                                        Constant.toast(this@ShowBookActivity,"No Data Found")
                                        binding.noData.visibility = View.VISIBLE
                                    }
                                    else
                                    {
                                        binding.noData.visibility = View.GONE
                                    }
                                    val adapter = BooksAdapter(this@ShowBookActivity,data.CONTENT.toMutableList())
                                    binding.recyclerView.adapter = adapter
                                }
                                else
                                {
                                    Constant.toast(this@ShowBookActivity,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@ShowBookActivity,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@ShowBookActivity,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<GetAllBooksResponseModel>, p1: Throwable) {
                        Constant.toast(this@ShowBookActivity,p1.message.toString())
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