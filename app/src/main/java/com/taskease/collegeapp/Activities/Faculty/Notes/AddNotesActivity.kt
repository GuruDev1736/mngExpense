package com.taskease.collegeapp.Activities.Faculty.Notes

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
import com.taskease.collegeapp.databinding.ActivityAddNotesBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddNotesActivity : AppCompatActivity() {

    private lateinit var binding : ActivityAddNotesBinding

    private lateinit var storage : FirebaseStorage
    private var fileUrl : String? = null
    private var ImageUrl : String? = null

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            uploadFile(uri)
        }
    }

    private val ImagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            uploadImage(uri)
        }
    }

    private fun uploadFile(uri: Uri) {
        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()

        storage.getReference("Notes").child(System.currentTimeMillis().toString())
            .putFile(uri).addOnSuccessListener { it->
                it.storage.downloadUrl.addOnSuccessListener {
                    progressDialog.dismiss()
                    fileUrl = it.toString()
                    binding.linkInput.setText(fileUrl)
                    Log.d("FILE_URL",fileUrl.toString())
                }.addOnFailureListener{
                    Constant.toast(this@AddNotesActivity,it.message.toString())
                    progressDialog.dismiss()
                }
            }.addOnFailureListener{
                Constant.toast(this@AddNotesActivity,it.message.toString())
                progressDialog.dismiss()
            }
    }

    private fun uploadImage(uri: Uri) {
        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()

        storage.getReference("Notes").child("image").child(System.currentTimeMillis().toString())
            .putFile(uri).addOnSuccessListener { it->
                it.storage.downloadUrl.addOnSuccessListener {
                    progressDialog.dismiss()
                    ImageUrl = it.toString()
                    binding.imageLinkInput.setText(ImageUrl)
                    Log.d("FILE_URL",ImageUrl.toString())
                }.addOnFailureListener{
                    Constant.toast(this@AddNotesActivity,it.message.toString())
                    progressDialog.dismiss()
                }
            }.addOnFailureListener{
                Constant.toast(this@AddNotesActivity,it.message.toString())
                progressDialog.dismiss()
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNotesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storage = FirebaseStorage.getInstance()

        binding.actionBar.back.setOnClickListener{
            finish()
        }
        binding.actionBar.title.text = "Add Notes"

        binding.upload.setOnClickListener{
            filePickerLauncher.launch("*/*")
        }

        binding.uploadImage.setOnClickListener{
            ImagePickerLauncher.launch("image/*")
        }

        binding.submit.setOnClickListener{

            val title = binding.titleInput.text.toString()
            val description = binding.descriptionInput.text.toString()
            val link = binding.linkInput.text.toString()
            val imageLink = binding.imageLinkInput.text.toString()

            if (Valid(title,description,link,imageLink))
            {
                CallCreateNotesApi(title,description,link,imageLink)
            }
        }

        val code = intent.getStringExtra("code").toString()
        val id = intent.getIntExtra("id",0)
        val title = intent.getStringExtra("title").toString()
        val description = intent.getStringExtra("description").toString()
        val link = intent.getStringExtra("link").toString()
        val imageLink = intent.getStringExtra("imageLink").toString()

        if (code.equals("update"))
        {
            binding.actionBar.title.text = "Update Notes"
            binding.submit.text = "Update"
            binding.titleInput.setText(title)
            binding.descriptionInput.setText(description)
            binding.linkInput.setText(link)
            binding.imageLinkInput.setText(imageLink)
            binding.submit.setOnClickListener{
                val title = binding.titleInput.text.toString()
                val description = binding.descriptionInput.text.toString()
                val link = binding.linkInput.text.toString()
                val imageLink = binding.imageLinkInput.text.toString()

                if (Valid(title,description,link,imageLink))
                {
                    CallUpdateNotesApi(title,description,link,imageLink)
                }
            }
        }
    }

    private fun CallUpdateNotesApi(title: String, description: String, link: String, imageLink: String) {

        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()

        try {
            val token = SharedPreferencesManager.getString(this, SharedPreferencesManager.TOKEN)
            val id = intent.getIntExtra("id",0)

            val model = CreateSpreadSheetRequestModel("NOTES",description,imageLink,link,title)
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
                                    Constant.toast(this@AddNotesActivity,data.MSG)
                                    finish()
                                }
                                else
                                {
                                    Constant.toast(this@AddNotesActivity,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@AddNotesActivity,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@AddNotesActivity,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<SingleSpreadSheetResponseModel>, p1: Throwable) {
                        Constant.toast(this@AddNotesActivity,p1.message.toString())
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

    private fun CallCreateNotesApi(title: String, description: String, link: String, imageLink: String) {

        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()

        try {
            val token = SharedPreferencesManager.getString(this, SharedPreferencesManager.TOKEN)
            val departmentId = SharedPreferencesManager.getInt(this, SharedPreferencesManager.DEPARTMENTID)
            val userId = SharedPreferencesManager.getInt(this, SharedPreferencesManager.USERID)
            val model = CreateSpreadSheetRequestModel("NOTES",description,imageLink,link,title)
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
                                    Constant.toast(this@AddNotesActivity,data.MSG)
                                    finish()
                                }
                                else
                                {
                                    Constant.toast(this@AddNotesActivity,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@AddNotesActivity,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@AddNotesActivity,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<SingleSpreadSheetResponseModel>, p1: Throwable) {
                        Constant.toast(this@AddNotesActivity,p1.message.toString())
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



    private fun Valid(title: String, description: String , link: String , imageLink : String ): Boolean {

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
        else if (imageLink.isEmpty()) {
            binding.imageLinkInput.error = "Image Link is required please upload the file"
            binding.imageLinkInput.requestFocus()
            return false
        }
        return true
    }
}