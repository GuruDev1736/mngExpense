package com.taskease.collegeapp.Activities.Hostel

import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import com.taskease.collegeapp.databinding.ActivityUploadMessBillBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UploadMessBillActivity : AppCompatActivity() {

    private lateinit var binding : ActivityUploadMessBillBinding
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

        storage.getReference("Mess").child(System.currentTimeMillis().toString())
            .putFile(uri).addOnSuccessListener { it->
                it.storage.downloadUrl.addOnSuccessListener {
                    progressDialog.dismiss()
                    fileUrl = it.toString()
                    binding.linkInput.setText(fileUrl)
                    Log.d("FILE_URL",fileUrl.toString())
                }.addOnFailureListener{
                    Constant.toast(this@UploadMessBillActivity,it.message.toString())
                    progressDialog.dismiss()
                }
            }.addOnFailureListener{
                Constant.toast(this@UploadMessBillActivity,it.message.toString())
                progressDialog.dismiss()
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadMessBillBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storage = FirebaseStorage.getInstance()
        binding.actionBar.title.text = "Upload Mess Bill"
        binding.actionBar.back.setOnClickListener {
            finish()
        }

        binding.upload.setOnClickListener{
            filePickerLauncher.launch("*/*")
        }

        binding.submit.setOnClickListener{

            val title = binding.titleInput.text.toString()
            val description = binding.descriptionInput.text.toString()
            val link = binding.linkInput.text.toString()

            if (Valid(title,description,link))
            {
                CallCreateSpreadSheetApi(title,description,link)
            }
        }
    }

    private fun CallCreateSpreadSheetApi(title: String, description: String, link: String) {

        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()

        try {
            val token = SharedPreferencesManager.getString(this, SharedPreferencesManager.TOKEN)
            val userId = SharedPreferencesManager.getInt(this, SharedPreferencesManager.USERID)
            val model = CreateSpreadSheetRequestModel("MESS",description,"",link,title)
            RetrofitInstance.getInstance().createBudget(token,userId,model)
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
                                    Constant.toast(this@UploadMessBillActivity,data.MSG)
                                    finish()
                                }
                                else
                                {
                                    Constant.toast(this@UploadMessBillActivity,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@UploadMessBillActivity,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@UploadMessBillActivity,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<SingleSpreadSheetResponseModel>, p1: Throwable) {
                        Constant.toast(this@UploadMessBillActivity,p1.message.toString())
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