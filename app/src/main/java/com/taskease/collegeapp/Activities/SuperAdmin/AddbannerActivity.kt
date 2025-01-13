package com.taskease.collegeapp.Activities.SuperAdmin

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.storage.FirebaseStorage
import com.taskease.collegeapp.Adapter.AllbannerAdapter
import com.taskease.collegeapp.Adapter.BannerAdapter
import com.taskease.collegeapp.Constants.Constant
import com.taskease.collegeapp.Constants.CustomProgressDialog
import com.taskease.collegeapp.Constants.SharedPreferencesManager
import com.taskease.collegeapp.Model.Request.AddBannerRequestModel
import com.taskease.collegeapp.Model.Request.CreateTimeTableRequestModel
import com.taskease.collegeapp.Model.Response.AddBannerResponseModel
import com.taskease.collegeapp.Model.Response.BannerResponseModel
import com.taskease.collegeapp.Model.Response.CreateTimeTableResponseModel
import com.taskease.collegeapp.R
import com.taskease.collegeapp.Retrofit.RetrofitInstance
import com.taskease.collegeapp.databinding.ActivityAddbannerBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddbannerActivity : AppCompatActivity() {

    private lateinit var binding : ActivityAddbannerBinding
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

        storage.getReference("Banner").child(System.currentTimeMillis().toString())
            .putFile(uri).addOnSuccessListener { it->
                it.storage.downloadUrl.addOnSuccessListener {
                    progressDialog.dismiss()
                    fileUrl = it.toString()
                    binding.linkInput.setText(fileUrl)
                    Log.d("FILE_URL",fileUrl.toString())
                }.addOnFailureListener{
                    Constant.toast(this@AddbannerActivity,it.message.toString())
                    progressDialog.dismiss()
                }
            }.addOnFailureListener{
                Constant.toast(this@AddbannerActivity,it.message.toString())
                progressDialog.dismiss()
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddbannerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        GetAllbanners()

        storage = FirebaseStorage.getInstance()
        binding.actionBar.back.setOnClickListener{
            finish()
        }
        binding.actionBar.title.text = "Add Banner"

        binding.upload.setOnClickListener{
            filePickerLauncher.launch("image/*")
        }

        binding.submit.setOnClickListener{
            val title = binding.titleInput.text.toString()
            val link = binding.linkInput.text.toString()

            if (Valid(title,link))
            {
                CallAddBannerApi(title,link)
            }
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this@AddbannerActivity)
    }

    private fun CallAddBannerApi(title: String, link: String) {

        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()

        try {
            val token = SharedPreferencesManager.getString(this, SharedPreferencesManager.TOKEN)
            val model = AddBannerRequestModel(link,title)
            RetrofitInstance.getInstance().createBanner(token,model)
                .enqueue(object : Callback<AddBannerResponseModel> {
                    override fun onResponse(
                        p0: Call<AddBannerResponseModel>,
                        p1: Response<AddBannerResponseModel>
                    ) {
                        if (p1.isSuccessful)
                        {
                            progressDialog.dismiss()
                            val data = p1.body()
                            if (data!=null)
                            {
                                if (data.STS == "200")
                                {
                                    Constant.toast(this@AddbannerActivity,data.MSG)
                                    GetAllbanners()
                                }
                                else
                                {
                                    Constant.toast(this@AddbannerActivity,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@AddbannerActivity,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@AddbannerActivity,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<AddBannerResponseModel>, p1: Throwable) {
                        Constant.toast(this@AddbannerActivity,p1.message.toString())
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

    private fun Valid(title: String, link: String): Boolean {

        if (title.isEmpty())
        {
            binding.titleInput.error = "Title is required"
            binding.titleInput.requestFocus()
            return false
        }
        else if (link.isEmpty()) {
            binding.linkInput.error = "Link is required please upload the file"
            binding.linkInput.requestFocus()
            return false
        }
        return true
    }

    private fun GetAllbanners() {
        val progressDialog = CustomProgressDialog(this@AddbannerActivity)
        progressDialog.show()

        try {
            val token = SharedPreferencesManager.getString(this@AddbannerActivity, SharedPreferencesManager.TOKEN)

            RetrofitInstance.getInstance().getBanners(token).enqueue(object : Callback<BannerResponseModel> {
                override fun onResponse(
                    p0: Call<BannerResponseModel>,
                    p1: Response<BannerResponseModel>
                ) {
                    if (p1.isSuccessful) {
                        progressDialog.dismiss()
                        val data = p1.body()
                        if (data != null && data.STS == "200") {

                            binding.recyclerView.adapter = AllbannerAdapter(this@AddbannerActivity,data.CONTENT.toMutableList())

                        } else {
                            Constant.toast(this@AddbannerActivity, data?.MSG ?: "Error")
                        }
                    } else {
                        Constant.toast(this@AddbannerActivity, p1.message().toString())
                        progressDialog.dismiss()
                    }
                }

                override fun onFailure(p0: Call<BannerResponseModel>, p1: Throwable) {
                    Constant.toast(this@AddbannerActivity, p1.message.toString())
                    progressDialog.dismiss()
                }
            })
        }catch (e : Exception)
        {
            Constant.toast(this@AddbannerActivity,"Something went wrong")
            e.printStackTrace()
            progressDialog.dismiss()
        }
    }
}