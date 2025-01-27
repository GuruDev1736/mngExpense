package com.taskease.collegeapp.Activities.Student.Lib

import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import com.taskease.collegeapp.Activities.Lib.ShowBookActivity
import com.taskease.collegeapp.Activities.Student.Hostel.AddAdmissionActivity
import com.taskease.collegeapp.Activities.Student.Hostel.ShowHostelAttendanceActivity
import com.taskease.collegeapp.Activities.Student.Hostel.ShowPenaltyActivity
import com.taskease.collegeapp.Adapter.DashboardAdapter
import com.taskease.collegeapp.Constants.Constant
import com.taskease.collegeapp.Constants.CustomProgressDialog
import com.taskease.collegeapp.Constants.SharedPreferencesManager
import com.taskease.collegeapp.Model.DashboardItem
import com.taskease.collegeapp.Model.Response.UniversalResponseModel
import com.taskease.collegeapp.R
import com.taskease.collegeapp.Retrofit.RetrofitInstance
import com.taskease.collegeapp.databinding.ActivityLibraryBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LibraryActivity : AppCompatActivity() {

    private lateinit var binding : ActivityLibraryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLibraryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.actionBar.title.text = "Library"
        binding.actionBar.back.setOnClickListener{
            finish()
        }
        val list = ArrayList<DashboardItem>()

        list.add(DashboardItem(R.drawable.book,"Books", ShowBookActivity::class.java))
        binding.recyclerView.layoutManager = GridLayoutManager(this@LibraryActivity,2, VERTICAL,false)
        binding.recyclerView.adapter = DashboardAdapter(this@LibraryActivity,list)
    }

    override fun onStart() {
        super.onStart()
        getStatusApi("LIB")
    }

    private fun getStatusApi(category: String) {
        val progressDialog = CustomProgressDialog(this@LibraryActivity)
        progressDialog.show()

        try {
            val token = SharedPreferencesManager.getString(this@LibraryActivity, SharedPreferencesManager.TOKEN)

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
                                binding.status.text = "Library is Open"
                                binding.status.setTextColor(Color.GREEN)
                            }
                            else
                            {
                                binding.status.text = "Library is Close"
                                binding.status.setTextColor(Color.RED)
                            }
                        } else {
                            Constant.toast(this@LibraryActivity, data?.MSG ?: "Error")
                        }
                    } else {
                        Constant.toast(this@LibraryActivity, p1.message().toString())
                        progressDialog.dismiss()
                    }
                }

                override fun onFailure(p0: Call<UniversalResponseModel>, p1: Throwable) {
                    Constant.toast(this@LibraryActivity, p1.message.toString())
                    progressDialog.dismiss()
                }
            })
        }catch (e : Exception)
        {
            Constant.toast(this@LibraryActivity,"Something went wrong")
            e.printStackTrace()
            progressDialog.dismiss()
        }
    }
}