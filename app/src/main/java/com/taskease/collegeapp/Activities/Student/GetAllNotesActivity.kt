package com.taskease.collegeapp.Activities.Student

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.taskease.collegeapp.Activities.Student.GetAllAssignmentActivity
import com.taskease.collegeapp.Adapter.SpreadSheetAdapter
import com.taskease.collegeapp.Constants.Constant
import com.taskease.collegeapp.Constants.CustomProgressDialog
import com.taskease.collegeapp.Constants.SharedPreferencesManager
import com.taskease.collegeapp.Model.Response.SpreadSheetResponseModel
import com.taskease.collegeapp.R
import com.taskease.collegeapp.Retrofit.RetrofitInstance
import com.taskease.collegeapp.databinding.ActivityGetAllNotesBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GetAllNotesActivity : AppCompatActivity() {

    private lateinit var binding : ActivityGetAllNotesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGetAllNotesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.actionBar.title.text = "Get All Notes"
        binding.actionBar.back.setOnClickListener{
            finish()
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)

    }

    override fun onStart() {
        super.onStart()
        CallGetAllNotes()
    }

    private fun CallGetAllNotes() {
        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()

        try {
            val token = SharedPreferencesManager.getString(this, SharedPreferencesManager.TOKEN)
            val departmentId = SharedPreferencesManager.getInt(this, SharedPreferencesManager.DEPARTMENTID)

            RetrofitInstance.getInstance().getAllSpreadSheetByCategoryAndDepartment(token,"NOTES",departmentId)
                .enqueue(object : Callback<SpreadSheetResponseModel> {
                    override fun onResponse(
                        p0: Call<SpreadSheetResponseModel>,
                        p1: Response<SpreadSheetResponseModel>
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
                                        Constant.toast(this@GetAllNotesActivity,"No Data Found")
                                        binding.noData.visibility = View.VISIBLE
                                    }
                                    else
                                    {
                                        binding.noData.visibility = View.GONE
                                    }
                                    val adapter = SpreadSheetAdapter(this@GetAllNotesActivity,data.CONTENT.toMutableList(),"ASSIGNMENT")
                                    binding.recyclerView.adapter = adapter
                                }
                                else
                                {
                                    Constant.toast(this@GetAllNotesActivity,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@GetAllNotesActivity,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@GetAllNotesActivity,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<SpreadSheetResponseModel>, p1: Throwable) {
                        Constant.toast(this@GetAllNotesActivity,p1.message.toString())
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