package com.taskease.collegeapp.Activities.Office

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.taskease.collegeapp.Adapter.BonafideAdapter
import com.taskease.collegeapp.Adapter.SpreadSheetAdapter
import com.taskease.collegeapp.Constants.Constant
import com.taskease.collegeapp.Constants.CustomProgressDialog
import com.taskease.collegeapp.Constants.SharedPreferencesManager
import com.taskease.collegeapp.Model.Request.CreateBonafideRequestModel
import com.taskease.collegeapp.Model.Request.CreateLostReportRequestModel
import com.taskease.collegeapp.Model.Response.Bonafide
import com.taskease.collegeapp.Model.Response.CreateBonafideResponseModel
import com.taskease.collegeapp.Model.Response.CreateLostReportResponseModel
import com.taskease.collegeapp.Model.Response.GetAllBonafideResponseModel
import com.taskease.collegeapp.Model.Response.SpreadSheetResponseModel
import com.taskease.collegeapp.R
import com.taskease.collegeapp.Retrofit.RetrofitInstance
import com.taskease.collegeapp.databinding.ActivityShowBonafideBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ShowBonafideActivity : AppCompatActivity() {

    private lateinit var binding : ActivityShowBonafideBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowBonafideBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.actionBar.back.setOnClickListener{
            finish()
        }

        binding.actionBar.title.text = "Bonafide Reports"
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        val role = SharedPreferencesManager.getString(this, SharedPreferencesManager.ROLE,"")

        if (role == "ROLE_STUDENT")
        {
            binding.create.visibility = View.VISIBLE
            CallGetAllBonafideByStudent()
        }

        binding.create.setOnClickListener{
            showCustomDialog()
        }

    }


    private fun showCustomDialog() {

        val dialogView = LayoutInflater.from(this@ShowBonafideActivity).inflate(R.layout.add_lost_report_dailog, null)
        val dialog = Dialog(this@ShowBonafideActivity)
        dialog.setContentView(dialogView)

        val title = dialogView.findViewById<EditText>(R.id.title)
        val description = dialogView.findViewById<EditText>(R.id.description)
        val link_layout = dialogView.findViewById<LinearLayout>(R.id.link_layout)
        val submit = dialogView.findViewById<Button>(R.id.submit)
        val close = dialogView.findViewById<Button>(R.id.close)

        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        link_layout.visibility = View.GONE

        close.setOnClickListener{
            dialog.dismiss()
        }

        submit.setOnClickListener{

            val bonafideTitle = title.text.toString()
            val bonafideDescription = description.text.toString()

            if(bonafideDescription.isEmpty() || bonafideDescription.isEmpty())
            {
                Constant.toast(this,"Please fill all the fields")
                return@setOnClickListener
            }
            else
            {
                CallCreateBonafide(bonafideTitle,bonafideDescription,dialog)
            }
        }


        dialog.show()
    }

    private fun CallCreateBonafide(bonafideTitle: String, bonafideDescription: String , dialog: Dialog ) {

        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()

        try {
            val token = SharedPreferencesManager.getString(this, SharedPreferencesManager.TOKEN)
            val userId = SharedPreferencesManager.getInt(this,SharedPreferencesManager.USERID)

            val model = CreateBonafideRequestModel(bonafideDescription,bonafideTitle)
            RetrofitInstance.getInstance().createBonafide(token,userId,model)
                .enqueue(object : Callback<CreateBonafideResponseModel> {
                    override fun onResponse(
                        p0: Call<CreateBonafideResponseModel>,
                        p1: Response<CreateBonafideResponseModel>
                    ) {
                        if (p1.isSuccessful)
                        {
                            progressDialog.dismiss()
                            val data = p1.body()
                            if (data!=null)
                            {
                                if (data.STS == "200")
                                {
                                    Constant.toast(this@ShowBonafideActivity,data.MSG)
                                    dialog.dismiss()
                                    CallGetAllBonafideByStudent()
                                }
                                else
                                {
                                    Constant.toast(this@ShowBonafideActivity,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@ShowBonafideActivity,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@ShowBonafideActivity,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<CreateBonafideResponseModel>, p1: Throwable) {
                        Constant.toast(this@ShowBonafideActivity,p1.message.toString())
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

    override fun onStart() {
        super.onStart()
        CallGetAllBonafideReport()
    }

    private fun CallGetAllBonafideReport() {
        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()

        try {
            val token = SharedPreferencesManager.getString(this, SharedPreferencesManager.TOKEN)

            RetrofitInstance.getInstance().getAllBonafide(token)
                .enqueue(object : Callback<GetAllBonafideResponseModel> {
                    override fun onResponse(
                        p0: Call<GetAllBonafideResponseModel>,
                        p1: Response<GetAllBonafideResponseModel>
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
                                        Constant.toast(this@ShowBonafideActivity,"No Data Found")
                                        binding.noData.visibility = View.VISIBLE
                                    }
                                    else
                                    {
                                        binding.noData.visibility = View.GONE
                                    }
                                    val adapter = BonafideAdapter(this@ShowBonafideActivity,data.CONTENT.toMutableList())
                                    binding.recyclerView.adapter = adapter
                                }
                                else
                                {
                                    Constant.toast(this@ShowBonafideActivity,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@ShowBonafideActivity,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@ShowBonafideActivity,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<GetAllBonafideResponseModel>, p1: Throwable) {
                        Constant.toast(this@ShowBonafideActivity,p1.message.toString())
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


    private fun CallGetAllBonafideByStudent() {
        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()

        try {
            val token = SharedPreferencesManager.getString(this, SharedPreferencesManager.TOKEN)
            val userId = SharedPreferencesManager.getInt(this, SharedPreferencesManager.USERID)

            RetrofitInstance.getInstance().getBonafideByStudent(token,userId)
                .enqueue(object : Callback<GetAllBonafideResponseModel> {
                    override fun onResponse(
                        p0: Call<GetAllBonafideResponseModel>,
                        p1: Response<GetAllBonafideResponseModel>
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
                                        Constant.toast(this@ShowBonafideActivity,"No Data Found")
                                        binding.noData.visibility = View.VISIBLE
                                    }
                                    else
                                    {
                                        binding.noData.visibility = View.GONE
                                    }
                                    val adapter = BonafideAdapter(this@ShowBonafideActivity,data.CONTENT.toMutableList())
                                    binding.recyclerView.adapter = adapter
                                }
                                else
                                {
                                    Constant.toast(this@ShowBonafideActivity,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@ShowBonafideActivity,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@ShowBonafideActivity,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<GetAllBonafideResponseModel>, p1: Throwable) {
                        Constant.toast(this@ShowBonafideActivity,p1.message.toString())
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