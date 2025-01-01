package com.taskease.collegeapp.Activities.Lib

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
import com.taskease.collegeapp.Model.Request.AddBookRequestModel
import com.taskease.collegeapp.Model.Request.CreateSpreadSheetRequestModel
import com.taskease.collegeapp.Model.Response.AddBookResponseModel
import com.taskease.collegeapp.Model.Response.SingleSpreadSheetResponseModel
import com.taskease.collegeapp.R
import com.taskease.collegeapp.Retrofit.RetrofitInstance
import com.taskease.collegeapp.databinding.ActivityAddBooksBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddBooksActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddBooksBinding
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

        storage.getReference("Books").child(System.currentTimeMillis().toString())
            .putFile(uri).addOnSuccessListener { it->
                it.storage.downloadUrl.addOnSuccessListener {
                    progressDialog.dismiss()
                    fileUrl = it.toString()
                    binding.bookLinkInput.setText(fileUrl)
                    Log.d("FILE_URL",fileUrl.toString())
                }.addOnFailureListener{
                    Constant.toast(this@AddBooksActivity,it.message.toString())
                    progressDialog.dismiss()
                }
            }.addOnFailureListener{
                Constant.toast(this@AddBooksActivity,it.message.toString())
                progressDialog.dismiss()
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBooksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storage = FirebaseStorage.getInstance()

        binding.actionBar.back.setOnClickListener{
            finish()
        }

        binding.actionBar.title.text = "Add Book"

        binding.submit.setOnClickListener{

            val title = binding.titleInput.text.toString()
            val description = binding.descriptionInput.text.toString()
            val author = binding.author.text.toString()
            val publisher = binding.publisherName.text.toString()
            val link = binding.bookLinkInput.text.toString()

            if (Valid(title,description,author,publisher,link))
            {
                CallCreateBookApi(title,description,author,publisher,link)
            }
        }

        binding.uploadBook.setOnClickListener{
            filePickerLauncher.launch("*/*")
        }

        val code = intent.getStringExtra("code").toString()
        val title = intent.getStringExtra("title").toString()
        val description = intent.getStringExtra("description").toString()
        val author = intent.getStringExtra("author").toString()
        val publisher = intent.getStringExtra("publisher").toString()
        val link = intent.getStringExtra("link").toString()

        if (code.equals("update"))
        {
            binding.actionBar.title.text = "Update Book"
            binding.submit.text = "Update"
            binding.titleInput.setText(title)
            binding.descriptionInput.setText(description)
            binding.author.setText(author)
            binding.publisherName.setText(publisher)
            binding.bookLinkInput.setText(link)
            binding.submit.setOnClickListener{
                val title = binding.titleInput.text.toString()
                val description = binding.descriptionInput.text.toString()
                val author = binding.author.text.toString()
                val publisher = binding.publisherName.text.toString()
                val link = binding.bookLinkInput.text.toString()

                if (Valid(title,description,author,publisher,link))
                {
                    CallUpdateAPI(title,description,author,publisher,link)
                }
            }
        }

    }

    private fun CallUpdateAPI(title: String, description: String, author: String , publisher : String, link: String) {

        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()

        try {
            val token = SharedPreferencesManager.getString(this, SharedPreferencesManager.TOKEN)
            val id = intent.getIntExtra("id",0)

            val model = AddBookRequestModel(author,link,description,publisher,title)
            RetrofitInstance.getInstance().updateBook(token,id,model)
                .enqueue(object : Callback<AddBookResponseModel> {
                    override fun onResponse(
                        p0: Call<AddBookResponseModel>,
                        p1: Response<AddBookResponseModel>
                    ) {
                        if (p1.isSuccessful)
                        {
                            progressDialog.dismiss()
                            val data = p1.body()
                            if (data!=null)
                            {
                                if (data.STS == "200")
                                {
                                    Constant.toast(this@AddBooksActivity,data.MSG)
                                    finish()
                                }
                                else
                                {
                                    Constant.toast(this@AddBooksActivity,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@AddBooksActivity,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@AddBooksActivity,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<AddBookResponseModel>, p1: Throwable) {
                        Constant.toast(this@AddBooksActivity,p1.message.toString())
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

    private fun CallCreateBookApi(title: String, description: String, author: String , publisher : String, link: String) {

        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()

        try {
            val token = SharedPreferencesManager.getString(this, SharedPreferencesManager.TOKEN)
            val userId = SharedPreferencesManager.getInt(this, SharedPreferencesManager.USERID)
            val model = AddBookRequestModel(author,link,description,publisher,title)
            RetrofitInstance.getInstance().createBook(token,userId,model)
                .enqueue(object : Callback<AddBookResponseModel> {
                    override fun onResponse(
                        p0: Call<AddBookResponseModel>,
                        p1: Response<AddBookResponseModel>
                    ) {
                        if (p1.isSuccessful)
                        {
                            progressDialog.dismiss()
                            val data = p1.body()
                            if (data!=null)
                            {
                                if (data.STS == "200")
                                {
                                    Constant.toast(this@AddBooksActivity,data.MSG)
                                    finish()
                                }
                                else
                                {
                                    Constant.toast(this@AddBooksActivity,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@AddBooksActivity,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@AddBooksActivity,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<AddBookResponseModel>, p1: Throwable) {
                        Constant.toast(this@AddBooksActivity,p1.message.toString())
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

    private fun Valid(title: String, description: String, author : String , publisher : String, link: String): Boolean {

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
            binding.bookLinkInput.error = "Link is required"
            binding.bookLinkInput.requestFocus()
            return false
        }
        else if(!link.startsWith("http://") && !link.startsWith("https://"))
        {
            binding.bookLinkInput.error = "Invalid Link"
            binding.bookLinkInput.requestFocus()
            return false
        }
        else if (author.isEmpty()) {
            binding.author.error = "Author is required"
            binding.author.requestFocus()
            return false
        }
        else if (publisher.isEmpty()) {
            binding.publisherName.error = "Publisher is required"
            binding.publisherName.requestFocus()
            return false
        }
        return true
    }
}