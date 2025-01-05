package com.taskease.collegeapp.Auth

import android.app.Activity
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
import com.google.firebase.storage.FirebaseStorage
import com.taskease.collegeapp.Constants.Constant
import com.taskease.collegeapp.Constants.CustomProgressDialog
import com.taskease.collegeapp.Constants.SharedPreferencesManager
import com.taskease.collegeapp.Model.Request.CreateSpreadSheetRequestModel
import com.taskease.collegeapp.Model.Request.CreateStudentRequestModel
import com.taskease.collegeapp.Model.Response.CreateStudentResponseModel
import com.taskease.collegeapp.Model.Response.GetAllBatchResponseModel
import com.taskease.collegeapp.Model.Response.GetAllDepartmentResponseModel
import com.taskease.collegeapp.Model.Response.GetAllYearResponseModel
import com.taskease.collegeapp.Model.Response.SingleSpreadSheetResponseModel
import com.taskease.collegeapp.R
import com.taskease.collegeapp.Retrofit.RetrofitInstance
import com.taskease.collegeapp.databinding.ActivityRegisterBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding : ActivityRegisterBinding
    private var departmentId: Int = -1
    private var yearId: Int = -1
    private var batchId: Int = -1
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

        storage.getReference("Notes").child("image").child(System.currentTimeMillis().toString())
            .putFile(uri).addOnSuccessListener { it->
                it.storage.downloadUrl.addOnSuccessListener {
                    progressDialog.dismiss()
                    ImageUrl = it.toString()
                    Log.d("FILE_URL",ImageUrl.toString())
                }.addOnFailureListener{
                    Constant.toast(this@RegisterActivity,it.message.toString())
                    progressDialog.dismiss()
                }
            }.addOnFailureListener{
                Constant.toast(this@RegisterActivity,it.message.toString())
                progressDialog.dismiss()
            }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storage = FirebaseStorage.getInstance()

        binding.actionBar.title.text = "Sign Up"
        binding.actionBar.back.setOnClickListener{
            finish()
        }

        binding.profilePic.setOnClickListener{
            ImagePickerLauncher.launch("image/*")
        }

        binding.btnSignup.setOnClickListener{

            val fullName = binding.etFullName.text.toString()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val phoneNo = binding.etPhoneNo.text.toString()
            val enrollment = binding.etEnrollmentNo.text.toString()

            if (Valid(fullName,email,password,phoneNo,enrollment,departmentId,yearId,batchId,ImageUrl))
            {
                callCreateStudentApi(fullName,email,password,phoneNo,enrollment,departmentId,yearId,batchId,ImageUrl)
            }

        }


    }

    private fun callCreateStudentApi(
        fullName: String,
        email: String,
        password: String,
        phoneNo: String,
        enrollment: String,
        departmentId: Int,
        yearId: Int,
        batchId: Int,
        imageUrl: String?
    ) {

        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()

        try {
            val model = CreateStudentRequestModel(email,enrollment,fullName,password,phoneNo,imageUrl.toString())
            RetrofitInstance.getInstance().addStudent(yearId,departmentId,batchId,model)
                .enqueue(object : Callback<CreateStudentResponseModel> {
                    override fun onResponse(
                        p0: Call<CreateStudentResponseModel>,
                        p1: Response<CreateStudentResponseModel>
                    ) {
                        if (p1.isSuccessful)
                        {
                            progressDialog.dismiss()
                            val data = p1.body()
                            if (data!=null)
                            {
                                if (data.STS == "200")
                                {
                                    Constant.toast(this@RegisterActivity,data.MSG)
                                    finish()
                                }
                                else
                                {
                                    Constant.toast(this@RegisterActivity,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@RegisterActivity,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@RegisterActivity,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<CreateStudentResponseModel>, p1: Throwable) {
                        Constant.toast(this@RegisterActivity,p1.message.toString())
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

    private fun Valid(
        fullName: String,
        email: String,
        password: String,
        phoneNo: String,
        enrollment: String,
        departmentId: Int?,
        yearId: Int?,
        batchId: Int?,
        imageUrl: String?
    ): Boolean {

        if (fullName.isEmpty())
        {
            binding.etFullName.error = "Full Name is required"
            binding.etFullName.requestFocus()
            return false
        }
        else if (email.isEmpty()) {
            binding.etEmail.error = "Email is required"
            binding.etEmail.requestFocus()
            return false
        }
        else if (password.isEmpty()) {
            binding.etPassword.error = "Password is required"
            binding.etPassword.requestFocus()
            return false
        }
        else if (phoneNo.isEmpty()) {
            binding.etPhoneNo.error = "Phone Number is required"
            binding.etPhoneNo.requestFocus()
            return false
        }
        else if (enrollment.isEmpty()) {
            binding.etEnrollmentNo.error = "Enrollment Number is required"
            binding.etEnrollmentNo.requestFocus()
            return false
        }
        else if (departmentId == -1)
        {
            Constant.toast(this,"Department is required")
            return false
        }
        else if (yearId == -1)
        {
            Constant.toast(this,"Year is required")
            return false
        }
        else if (batchId == -1)
        {
            Constant.toast(this,"Batch is required")
            return false
        }
        else if (imageUrl == null)
        {
            Constant.toast(this,"Profile Picture is required")
            return false
        }
        return true
    }

    override fun onStart() {
        super.onStart()
        GetAllYears()
        GetAllDepartments()
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
                                    val departmentNames = departmentList.map { it.name } // Extract names

                                    // Create an ArrayAdapter for the Spinner
                                    val adapter = ArrayAdapter(
                                        this@RegisterActivity,
                                        android.R.layout.simple_spinner_item,
                                        departmentNames
                                    )
                                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                                    binding.department.adapter = adapter

                                    // Set listener to get the selected department's ID
                                    binding.department.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                        override fun onItemSelected(
                                            parentView: AdapterView<*>,
                                            selectedItemView: View?,
                                            position: Int,
                                            id: Long
                                        ) {
                                            val selectedDepartmentId = departmentList[position].id
                                            Log.d(
                                                "Selected Department ID",
                                                selectedDepartmentId.toString()
                                            )

                                            departmentId = selectedDepartmentId

                                            GetAllbatches(selectedDepartmentId)
                                        }

                                        override fun onNothingSelected(parentView: AdapterView<*>) {

                                        }
                                    }
                                }
                                else
                                {
                                    Constant.toast(this@RegisterActivity,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@RegisterActivity,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@RegisterActivity,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<GetAllDepartmentResponseModel>, p1: Throwable) {
                        Constant.toast(this@RegisterActivity,p1.message.toString())
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

    private fun GetAllbatches(departmentId : Int) {
        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()

        try {
            RetrofitInstance.getInstance().getAllBatches(departmentId)
                .enqueue(object : Callback<GetAllBatchResponseModel> {
                    override fun onResponse(
                        p0: Call<GetAllBatchResponseModel>,
                        p1: Response<GetAllBatchResponseModel>
                    ) {
                        if (p1.isSuccessful)
                        {
                            progressDialog.dismiss()
                            val data = p1.body()
                            if (data!=null)
                            {
                                if (data.STS == "200")
                                {
                                    val batchList = data.CONTENT
                                    val batchNames = batchList.map { it.batchName } // Extract names

                                    // Create an ArrayAdapter for the Spinner
                                    val adapter = ArrayAdapter(
                                        this@RegisterActivity,
                                        android.R.layout.simple_spinner_item,
                                        batchNames
                                    )
                                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                                    binding.batch.adapter = adapter

                                    // Set listener to get the selected department's ID
                                    binding.batch.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                        override fun onItemSelected(
                                            parentView: AdapterView<*>,
                                            selectedItemView: View?,
                                            position: Int,
                                            id: Long
                                        ) {
                                            val selectedBatchId = batchList[position].id
                                            Log.d(
                                                "Selected Batch ID",
                                                selectedBatchId.toString()
                                            )
                                            batchId = selectedBatchId
                                        }

                                        override fun onNothingSelected(parentView: AdapterView<*>) {

                                        }
                                    }
                                }
                                else
                                {
                                    Constant.toast(this@RegisterActivity,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@RegisterActivity,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@RegisterActivity,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<GetAllBatchResponseModel>, p1: Throwable) {
                        Constant.toast(this@RegisterActivity,p1.message.toString())
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
                                        this@RegisterActivity,
                                        android.R.layout.simple_spinner_item,
                                        yearNames
                                    )
                                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                                    binding.year.adapter = adapter

                                    // Set listener to get the selected department's ID
                                    binding.year.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                        override fun onItemSelected(
                                            parentView: AdapterView<*>,
                                            selectedItemView: View?,
                                            position: Int,
                                            id: Long
                                        ) {
                                            val selectedDepartmentId = yearList[position].id
                                            Log.d(
                                                "Selected Year ID",
                                                selectedDepartmentId.toString()
                                            )
                                            yearId = selectedDepartmentId
                                        }

                                        override fun onNothingSelected(parentView: AdapterView<*>) {

                                        }
                                    }
                                }
                                else
                                {
                                    Constant.toast(this@RegisterActivity,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@RegisterActivity,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@RegisterActivity,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<GetAllYearResponseModel>, p1: Throwable) {
                        Constant.toast(this@RegisterActivity,p1.message.toString())
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