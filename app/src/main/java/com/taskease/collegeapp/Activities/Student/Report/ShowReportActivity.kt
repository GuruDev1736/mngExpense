package com.taskease.collegeapp.Activities.Student.Report

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.storage.FirebaseStorage
import com.taskease.collegeapp.Adapter.ReportAdapter
import com.taskease.collegeapp.Adapter.SpreadSheetAdapter
import com.taskease.collegeapp.Constants.Constant
import com.taskease.collegeapp.Constants.CustomProgressDialog
import com.taskease.collegeapp.Constants.SharedPreferencesManager
import com.taskease.collegeapp.Model.Request.AddBannerRequestModel
import com.taskease.collegeapp.Model.Request.CreateLostReportRequestModel
import com.taskease.collegeapp.Model.Response.AddBannerResponseModel
import com.taskease.collegeapp.Model.Response.CreateLostReportResponseModel
import com.taskease.collegeapp.Model.Response.GetAllReportResponseModel
import com.taskease.collegeapp.Model.Response.SpreadSheetResponseModel
import com.taskease.collegeapp.R
import com.taskease.collegeapp.Retrofit.RetrofitInstance
import com.taskease.collegeapp.databinding.ActivityShowReportBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ShowReportActivity : AppCompatActivity() {

    private lateinit var binding : ActivityShowReportBinding
    private lateinit var storage : FirebaseStorage
    private var fileUrl : String? = null
    private lateinit var link : EditText

    val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            uploadImage(uri , link)
        }
    }

    private fun uploadImage(uri: Uri , editText: EditText) {
        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()

        storage.getReference("Lost").child(System.currentTimeMillis().toString())
            .putFile(uri).addOnSuccessListener { it->
                it.storage.downloadUrl.addOnSuccessListener {
                    progressDialog.dismiss()
                    fileUrl = it.toString()
                    editText.setText(fileUrl)
                    Log.d("FILE_URL",fileUrl.toString())
                }.addOnFailureListener{
                    Constant.toast(this@ShowReportActivity,it.message.toString())
                    progressDialog.dismiss()
                }
            }.addOnFailureListener{
                Constant.toast(this@ShowReportActivity,it.message.toString())
                progressDialog.dismiss()
            }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowReportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        storage = FirebaseStorage.getInstance()
        binding.actionBar.title.text = "Lost Report"
        binding.actionBar.back.setOnClickListener{
            finish()
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        binding.create.setOnClickListener{
            showCustomDialog()
        }

        binding.history.setOnClickListener{
            startActivity(Intent(this,ShowReportHistoryActivity::class.java))
        }
    }


    private fun showCustomDialog() {

        val dialogView = LayoutInflater.from(this).inflate(R.layout.add_lost_report_dailog, null)
        val dialog = Dialog(this)
        dialog.setContentView(dialogView)

        val title = dialogView.findViewById<EditText>(R.id.title)
        val description = dialogView.findViewById<EditText>(R.id.description)
        val submit = dialogView.findViewById<Button>(R.id.submit)
        val close = dialogView.findViewById<Button>(R.id.close)
        val upload = dialogView.findViewById<Button>(R.id.upload)
        link = dialogView.findViewById(R.id.link_input)


        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        upload.setOnClickListener{
            filePickerLauncher.launch("image/*")
        }

        submit.setOnClickListener {
            val title = title.text.toString()
            val descrip = description.text.toString()
            if (title.isEmpty() || descrip.isEmpty() || fileUrl == null) {
                Constant.toast(this, "Please Enter All the details")
            } else {
                CallAddReportApi(title , descrip , fileUrl , dialog)
            }
        }

        close.setOnClickListener{
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun CallAddReportApi(title: String, descrip: String, fileUrl: String?, dialog: Dialog) {

        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()

        try {
            val token = SharedPreferencesManager.getString(this, SharedPreferencesManager.TOKEN)
            val userId = SharedPreferencesManager.getInt(this,SharedPreferencesManager.USERID)
            val model = CreateLostReportRequestModel(descrip,fileUrl.toString(),"LOST",title)
            RetrofitInstance.getInstance().createLostReport(token,model,userId)
                .enqueue(object : Callback<CreateLostReportResponseModel> {
                    override fun onResponse(
                        p0: Call<CreateLostReportResponseModel>,
                        p1: Response<CreateLostReportResponseModel>
                    ) {
                        if (p1.isSuccessful)
                        {
                            progressDialog.dismiss()
                            val data = p1.body()
                            if (data!=null)
                            {
                                if (data.STS == "200")
                                {
                                    Constant.toast(this@ShowReportActivity,data.MSG)
                                    dialog.dismiss()
                                    getAllReports()
                                }
                                else
                                {
                                    Constant.toast(this@ShowReportActivity,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@ShowReportActivity,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@ShowReportActivity,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<CreateLostReportResponseModel>, p1: Throwable) {
                        Constant.toast(this@ShowReportActivity,p1.message.toString())
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
        getAllReports()
    }

    private fun getAllReports() {
        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()

        try {
            val token = SharedPreferencesManager.getString(this, SharedPreferencesManager.TOKEN)

            RetrofitInstance.getInstance().getAllReport(token)
                .enqueue(object : Callback<GetAllReportResponseModel> {
                    override fun onResponse(
                        p0: Call<GetAllReportResponseModel>,
                        p1: Response<GetAllReportResponseModel>
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
                                        Constant.toast(this@ShowReportActivity,"No Data Found")
                                        binding.noData.visibility = View.VISIBLE
                                    }
                                    else
                                    {
                                        binding.noData.visibility = View.GONE
                                    }
                                    val adapter = ReportAdapter(this@ShowReportActivity,data.CONTENT.toMutableList() , "REPORT")
                                    binding.recyclerView.adapter = adapter
                                }
                                else
                                {
                                    Constant.toast(this@ShowReportActivity,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@ShowReportActivity,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@ShowReportActivity,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<GetAllReportResponseModel>, p1: Throwable) {
                        Constant.toast(this@ShowReportActivity,p1.message.toString())
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