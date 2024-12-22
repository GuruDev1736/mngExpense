package com.taskease.collegeapp.Activities.Faculty.Attendance

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.storage.FirebaseStorage
import com.taskease.collegeapp.Constants.Constant
import com.taskease.collegeapp.Constants.CustomProgressDialog
import com.taskease.collegeapp.Constants.SharedPreferencesManager
import com.taskease.collegeapp.Model.Request.CreateSpreadSheetRequestModel
import com.taskease.collegeapp.Model.Response.SingleSpreadSheetResponseModel
import com.taskease.collegeapp.R
import com.taskease.collegeapp.Retrofit.RetrofitInstance
import com.taskease.collegeapp.databinding.ActivityAddAttendanceBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddAttendanceActivity : AppCompatActivity() {

    private lateinit var binding : ActivityAddAttendanceBinding
    private lateinit var storage : FirebaseStorage
    private var fileUrl : String? = null

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            uploadImage(uri)
        }
    }

    private fun uploadImage(uri: Uri) {
        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()

        storage.getReference("Attendance").child(System.currentTimeMillis().toString())
            .putFile(uri).addOnSuccessListener { it->
                it.storage.downloadUrl.addOnSuccessListener {
                    progressDialog.dismiss()
                    fileUrl = it.toString()
                    binding.linkInput.setText(fileUrl)
                    Log.d("FILE_URL",fileUrl.toString())
                }.addOnFailureListener{
                    Constant.toast(this@AddAttendanceActivity,it.message.toString())
                    progressDialog.dismiss()
                }
            }.addOnFailureListener{
                Constant.toast(this@AddAttendanceActivity,it.message.toString())
                progressDialog.dismiss()
            }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storage = FirebaseStorage.getInstance()

        binding.actionBar.back.setOnClickListener{
            finish()
        }
        binding.actionBar.title.text = "Add Attendance"

        binding.upload.setOnClickListener{
            filePickerLauncher.launch("*/*")
        }

        val adapter = ArrayAdapter.createFromResource(this@AddAttendanceActivity,R.array.category,android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.category.adapter = adapter

        binding.submit.setOnClickListener{

            val title = binding.titleInput.text.toString()
            val description = binding.descriptionInput.text.toString()
            val link = binding.linkInput.text.toString()
            val category = binding.category.selectedItem.toString()

            if (Valid(title,description,link))
            {
                CallCreateSpreadSheetApi(title,description,link,category)
            }
        }

        val code = intent.getStringExtra("code").toString()
        val id = intent.getIntExtra("id",0)
        val title = intent.getStringExtra("title").toString()
        val description = intent.getStringExtra("description").toString()
        val link = intent.getStringExtra("link").toString()
        val category = intent.getStringExtra("category").toString()

        if (code.equals("update"))
        {
            binding.actionBar.title.text = "Update Attendance"
            binding.submit.text = "Update"
            binding.titleInput.setText(title)
            binding.descriptionInput.setText(description)
            binding.linkInput.setText(link)
            if (category.equals("TEACHER"))
            {
                binding.category.setSelection(0)
            }
            if(category.equals("STUDENT"))
            {
                binding.category.setSelection(1)
            }

            binding.submit.setOnClickListener{
                val title = binding.titleInput.text.toString()
                val description = binding.descriptionInput.text.toString()
                val link = binding.linkInput.text.toString()
                val category = binding.category.selectedItem.toString()

                if (Valid(title,description,link))
                {
                    CallUpdateAttendanceApi(title,description,link,category)
                }
            }
        }


    }


    private fun CallUpdateAttendanceApi(title: String, description: String, link: String,category: String) {
        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()
        try {
            val token = SharedPreferencesManager.getString(this, SharedPreferencesManager.TOKEN)
            val id = intent.getIntExtra("id",0)

            val model = CreateSpreadSheetRequestModel(category,description,"",link,title)
            RetrofitInstance.getInstance().updateSheet(token,id,model)
                .enqueue(object : Callback<SingleSpreadSheetResponseModel> {
                    override fun onResponse(
                        p0: Call<SingleSpreadSheetResponseModel>,
                        p1: Response<SingleSpreadSheetResponseModel>
                    ) {
                        if (p1.isSuccessful)
                        {
                            progressDialog.dismiss()
                            val data = p1.body()
                            if (data!=null)
                            {
                                if (data.STS == "200")
                                {
                                    Constant.toast(this@AddAttendanceActivity,data.MSG)
                                    finish()
                                }
                                else
                                {
                                    Constant.toast(this@AddAttendanceActivity,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@AddAttendanceActivity,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@AddAttendanceActivity,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<SingleSpreadSheetResponseModel>, p1: Throwable) {
                        Constant.toast(this@AddAttendanceActivity,p1.message.toString())
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

    private fun CallCreateSpreadSheetApi(title: String, description: String, link: String,category: String) {

        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()

        try {
            val token = SharedPreferencesManager.getString(this, SharedPreferencesManager.TOKEN)
            val departmentId = SharedPreferencesManager.getInt(this, SharedPreferencesManager.DEPARTMENTID)
            val userId = SharedPreferencesManager.getInt(this, SharedPreferencesManager.USERID)
            val model = CreateSpreadSheetRequestModel(category,description,"",link,title)
            RetrofitInstance.getInstance().createSpreadSheet(token,departmentId,userId,model)
                .enqueue(object : Callback<SingleSpreadSheetResponseModel> {
                    override fun onResponse(
                        p0: Call<SingleSpreadSheetResponseModel>,
                        p1: Response<SingleSpreadSheetResponseModel>
                    ) {
                        if (p1.isSuccessful)
                        {
                            progressDialog.dismiss()
                            val data = p1.body()
                            if (data!=null)
                            {
                                if (data.STS == "200")
                                {
                                    Constant.toast(this@AddAttendanceActivity,data.MSG)
                                    finish()
                                }
                                else
                                {
                                    Constant.toast(this@AddAttendanceActivity,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@AddAttendanceActivity,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@AddAttendanceActivity,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<SingleSpreadSheetResponseModel>, p1: Throwable) {
                        Constant.toast(this@AddAttendanceActivity,p1.message.toString())
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


    private fun Valid(title: String, description: String , link: String): Boolean {

        if (title.isEmpty())
        {
            binding.titleInput.error = "Title is required"
            binding.titleInput.requestFocus()
            return false
        }
        else if (description.isEmpty()) {
            binding.descriptionInput.error = "Description is required"
            binding.descriptionInput.requestFocus()
            return false
        }
        else if (link.isEmpty()) {
            binding.linkInput.error = "Link is required please upload the file"
            binding.linkInput.requestFocus()
            return false
        }
        return true
    }
}