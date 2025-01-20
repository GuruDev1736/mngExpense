package com.taskease.collegeapp.Activities.Student.Hostel

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.taskease.collegeapp.Constants.Constant
import com.taskease.collegeapp.Constants.CustomProgressDialog
import com.taskease.collegeapp.Constants.SharedPreferencesManager
import com.taskease.collegeapp.Model.Request.AddAdmissionRequestModel
import com.taskease.collegeapp.Model.Response.AddAdmissionResponseModel
import com.taskease.collegeapp.Model.Response.GetAllDepartmentResponseModel
import com.taskease.collegeapp.Model.Response.GetAllYearResponseModel
import com.taskease.collegeapp.R
import com.taskease.collegeapp.Retrofit.RetrofitInstance
import com.taskease.collegeapp.databinding.ActivityAddAdmissionBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddAdmissionActivity : AppCompatActivity() {

    private lateinit var binding : ActivityAddAdmissionBinding
    private var ImageUrl : String? = null
    private lateinit var storage : FirebaseStorage
    private val ImagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            uploadImage(uri)
        }
    }


    private fun uploadImage(uri: Uri) {
        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()

        storage.getReference("admission").child("aadhar").child(System.currentTimeMillis().toString())
            .putFile(uri).addOnSuccessListener { it->
                it.storage.downloadUrl.addOnSuccessListener {
                    progressDialog.dismiss()
                    ImageUrl = it.toString()
                    binding.linkInput.setText(ImageUrl)
                    Log.d("FILE_URL",ImageUrl.toString())
                }.addOnFailureListener{
                    Constant.toast(this@AddAdmissionActivity,it.message.toString())
                    progressDialog.dismiss()
                }
            }.addOnFailureListener{
                Constant.toast(this@AddAdmissionActivity,it.message.toString())
                progressDialog.dismiss()
            }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddAdmissionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        storage = FirebaseStorage.getInstance()

        binding.actionBar.title.text = "Admission"
        binding.actionBar.back.setOnClickListener{
            finish()
        }

        binding.upload.setOnClickListener{
            ImagePickerLauncher.launch("image/*")
        }

        binding.submit.setOnClickListener{
            val name = binding.name.text.toString()
            val department = binding.department.selectedItem.toString()
            val year = binding.year.selectedItem.toString()
            val address = binding.address.text.toString()
            val studentPhoneNo = binding.studePhone.text.toString()
            val parentPhoneNo = binding.parentPhone.text.toString()
            val percentage = binding.schoolPercentage.text.toString()
            val diplomaPercentage = binding.diplomaPercentage.text.toString()
            val link = binding.linkInput.text.toString()

            if (valid(name,department,year,address,studentPhoneNo,parentPhoneNo,percentage,diplomaPercentage,link))
            {
                CallAddAdmission(name,department,year,address,studentPhoneNo,parentPhoneNo,percentage,diplomaPercentage,link)
            }
        }
    }

    private fun CallAddAdmission(
        name: String,
        department: String,
        year: String,
        address: String,
        studentPhoneNo: String,
        parentPhoneNo: String,
        percentage: String,
        diplomaPercentage: String,
        link: String
    ) {
        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()

        val token = SharedPreferencesManager.getString(this,SharedPreferencesManager.TOKEN)
        val userId = SharedPreferencesManager.getInt(this,SharedPreferencesManager.USERID)
        val model = AddAdmissionRequestModel(link, address,department,diplomaPercentage,parentPhoneNo,percentage,name,studentPhoneNo,year)
        try {
            RetrofitInstance.getInstance().addAdmission(token,userId,model)
                .enqueue(object : Callback<AddAdmissionResponseModel> {
                    override fun onResponse(
                        p0: Call<AddAdmissionResponseModel>,
                        p1: Response<AddAdmissionResponseModel>
                    ) {
                        if (p1.isSuccessful)
                        {
                            progressDialog.dismiss()
                            val data = p1.body()
                            if (data!=null)
                            {
                                if (data.STS == "200")
                                {
                                    Constant.toast(this@AddAdmissionActivity,data.MSG)
                                    finish()
                                }
                                else
                                {
                                    Constant.toast(this@AddAdmissionActivity,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@AddAdmissionActivity,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@AddAdmissionActivity,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<AddAdmissionResponseModel>, p1: Throwable) {
                        Constant.toast(this@AddAdmissionActivity,p1.message.toString())
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
        GetAllDepartments()
        GetAllYears()
    }

    private fun valid(
        name: String,
        department: String,
        year: String,
        address: String,
        studentPhoneNo: String,
        parentPhoneNo: String,
        percentage: String,
        diplomaPercentage: String,
        link: String
    ): Boolean {

        if (name.isEmpty())
        {
            binding.name.error = "Name is required"
            binding.name.requestFocus()
            return false
        }
        else if (department.isEmpty()) {
            Constant.toast(this,"Please Select the department")
            binding.department.requestFocus()
            return false
        }
        else if (year.isEmpty()) {
            Constant.toast(this,"Please select the year")
            binding.year.requestFocus()
            return false
        }
        else if (address.isEmpty()) {
            binding.address.error = "Description is required"
            binding.address.requestFocus()
            return false
        }
        else if (studentPhoneNo.isEmpty()) {
            binding.studePhone.error = "Student Phone Number is required"
            binding.studePhone.requestFocus()
            return false
        }
        else if (parentPhoneNo.isEmpty()) {
            binding.parentPhone.error = "Parent Phone Number is required"
            binding.parentPhone.requestFocus()
            return false
        }
        else if (percentage.isEmpty()) {
            binding.schoolPercentage.error = "School Percentage is required"
            binding.schoolPercentage.requestFocus()
            return false
        }
        else if (diplomaPercentage.isEmpty()) {
            binding.diplomaPercentage.error = "Diploma Percentage is required"
            binding.diplomaPercentage.requestFocus()
            return false
        }
        else if (link.isEmpty()) {
            binding.linkInput.error = "Link is required"
            binding.linkInput.requestFocus()
            return false
        }
        return true
    }


    private fun GetAllDepartments() {
        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()

        try {
            RetrofitInstance.getInstance().getAllDepartments()
                .enqueue(object : Callback<GetAllDepartmentResponseModel> {
                    override fun onResponse(
                        p0: Call<GetAllDepartmentResponseModel>,
                        p1: Response<GetAllDepartmentResponseModel>
                    ) {
                        if (p1.isSuccessful)
                        {
                            progressDialog.dismiss()
                            val data = p1.body()
                            if (data!=null)
                            {
                                if (data.STS == "200")
                                {
                                    val departmentList = data.CONTENT
                                    val departmentNames = departmentList.map { it.name }
                                    val adapter = ArrayAdapter(
                                        this@AddAdmissionActivity,
                                        android.R.layout.simple_spinner_item,
                                        departmentNames
                                    )
                                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                    binding.department.adapter = adapter
                                }
                                else
                                {
                                    Constant.toast(this@AddAdmissionActivity,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@AddAdmissionActivity,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@AddAdmissionActivity,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<GetAllDepartmentResponseModel>, p1: Throwable) {
                        Constant.toast(this@AddAdmissionActivity,p1.message.toString())
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


    private fun GetAllYears() {
        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()

        try {
            RetrofitInstance.getInstance().getAllYears()
                .enqueue(object : Callback<GetAllYearResponseModel> {
                    override fun onResponse(
                        p0: Call<GetAllYearResponseModel>,
                        p1: Response<GetAllYearResponseModel>
                    ) {
                        if (p1.isSuccessful)
                        {
                            progressDialog.dismiss()
                            val data = p1.body()
                            if (data!=null)
                            {
                                if (data.STS == "200")
                                {
                                    val yearList = data.CONTENT
                                    val yearNames = yearList.map { it.year } // Extract names

                                    // Create an ArrayAdapter for the Spinner
                                    val adapter = ArrayAdapter(
                                        this@AddAdmissionActivity,
                                        android.R.layout.simple_spinner_item,
                                        yearNames
                                    )
                                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                                    binding.year.adapter = adapter
                                }
                                else
                                {
                                    Constant.toast(this@AddAdmissionActivity,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@AddAdmissionActivity,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@AddAdmissionActivity,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<GetAllYearResponseModel>, p1: Throwable) {
                        Constant.toast(this@AddAdmissionActivity,p1.message.toString())
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