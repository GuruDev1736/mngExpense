package com.taskease.collegeapp.Activities.SuperAdmin

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
import com.google.firebase.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.taskease.collegeapp.Constants.Constant
import com.taskease.collegeapp.Constants.CustomProgressDialog
import com.taskease.collegeapp.Constants.SharedPreferencesManager
import com.taskease.collegeapp.Model.Request.CreateUserRequestModel
import com.taskease.collegeapp.Model.Response.GetAllDepartmentResponseModel
import com.taskease.collegeapp.Model.Response.GetAllRoleResponseModel
import com.taskease.collegeapp.Model.Response.UniversalResponseModel
import com.taskease.collegeapp.Model.Response.UserUniversalResponseModel
import com.taskease.collegeapp.Model.Response.Users
import com.taskease.collegeapp.R
import com.taskease.collegeapp.Retrofit.RetrofitInstance
import com.taskease.collegeapp.databinding.ActivityCreateUserBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreateUserActivity : AppCompatActivity() {

    private lateinit var binding : ActivityCreateUserBinding
    private lateinit var storage : FirebaseStorage
    private var ImageUrl : String? = null
    private var selectedRole = ""
    private var departmentId: Int = -1

    private val ImagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            uploadImage(uri)
            Glide.with(this@CreateUserActivity).load(uri).into(binding.profilePic)
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
                    Constant.toast(this@CreateUserActivity,it.message.toString())
                    progressDialog.dismiss()
                }
            }.addOnFailureListener{
                Constant.toast(this@CreateUserActivity,it.message.toString())
                progressDialog.dismiss()
            }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        storage = FirebaseStorage.getInstance()

        binding.profilePic.setOnClickListener{
            ImagePickerLauncher.launch("image/*")
        }

        binding.actionBar.back.setOnClickListener{
            finish()
        }
        
        binding.actionBar.title.text = "Create User"

        binding.submit.setOnClickListener{
            val fullName = binding.etFullName.text.toString()
            val phoneNo = binding.etPhoneNo.text.toString()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val address = binding.etAddress.text.toString()
            val role = selectedRole
            val department = departmentId

            if (Valid(fullName, phoneNo, email, password, address)) {
                val userModel = CreateUserRequestModel(email,fullName,password,phoneNo,address,ImageUrl.toString())
                val token = SharedPreferencesManager.getString(this, SharedPreferencesManager.TOKEN)

                when (role) {
                    "ROLE_SUPER_ADMIN" -> {
                        callCreateSuperAdmin(userModel)
                    }
                    "ROLE_PRINCIPLE" -> {
                        callCreatePrinciple(userModel,token)
                    }
                    "ROLE_FACULTY" -> {
                        callCreateFaculty(userModel , department , token)
                    }
                    "ROLE_LIB" -> {
                        callCreateLib(userModel , token)
                    }
                    "ROLE_COOPERATIVE" -> {
                        callCreateCooperative(userModel , token)
                    }
                    "ROLE_WARDEN" -> {
                        callCreateWarden(userModel , token)
                    }
                    "ROLE_OFFICE" -> {
                        callCreateOffice(userModel , token)
                    }
                }
            }
        }
    }

    private fun callCreateOffice(userModel: CreateUserRequestModel , token : String) {

        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()

        try {
            RetrofitInstance.getInstance().createOffice(token,userModel)
                .enqueue(object : Callback<UserUniversalResponseModel> {
                    override fun onResponse(
                        p0: Call<UserUniversalResponseModel>,
                        p1: Response<UserUniversalResponseModel>
                    ) {
                        if (p1.isSuccessful)
                        {
                            progressDialog.dismiss()
                            val data = p1.body()
                            if (data!=null)
                            {
                                if (data.STS == "200")
                                {
                                    Constant.toast(this@CreateUserActivity,data.MSG)
                                    finish()
                                }
                                else
                                {
                                    Constant.toast(this@CreateUserActivity,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@CreateUserActivity,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@CreateUserActivity,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<UserUniversalResponseModel>, p1: Throwable) {
                        Constant.toast(this@CreateUserActivity,p1.message.toString())
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

    private fun callCreateWarden(userModel: CreateUserRequestModel , token : String) {
        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()

        try {
            RetrofitInstance.getInstance().createWarden(token,userModel)
                .enqueue(object : Callback<UserUniversalResponseModel> {
                    override fun onResponse(
                        p0: Call<UserUniversalResponseModel>,
                        p1: Response<UserUniversalResponseModel>
                    ) {
                        if (p1.isSuccessful)
                        {
                            progressDialog.dismiss()
                            val data = p1.body()
                            if (data!=null)
                            {
                                if (data.STS == "200")
                                {
                                    Constant.toast(this@CreateUserActivity,data.MSG)
                                    finish()
                                }
                                else
                                {
                                    Constant.toast(this@CreateUserActivity,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@CreateUserActivity,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@CreateUserActivity,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<UserUniversalResponseModel>, p1: Throwable) {
                        Constant.toast(this@CreateUserActivity,p1.message.toString())
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

    private fun callCreateCooperative(userModel: CreateUserRequestModel , token : String) {
        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()

        try {
            RetrofitInstance.getInstance().createCooperative(token , userModel)
                .enqueue(object : Callback<UserUniversalResponseModel> {
                    override fun onResponse(
                        p0: Call<UserUniversalResponseModel>,
                        p1: Response<UserUniversalResponseModel>
                    ) {
                        if (p1.isSuccessful)
                        {
                            progressDialog.dismiss()
                            val data = p1.body()
                            if (data!=null)
                            {
                                if (data.STS == "200")
                                {
                                    Constant.toast(this@CreateUserActivity,data.MSG)
                                    finish()
                                }
                                else
                                {
                                    Constant.toast(this@CreateUserActivity,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@CreateUserActivity,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@CreateUserActivity,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<UserUniversalResponseModel>, p1: Throwable) {
                        Constant.toast(this@CreateUserActivity,p1.message.toString())
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

    private fun callCreateLib(userModel: CreateUserRequestModel , token : String) {

        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()

        try {
            RetrofitInstance.getInstance().createLib(token , userModel)
                .enqueue(object : Callback<UserUniversalResponseModel> {
                    override fun onResponse(
                        p0: Call<UserUniversalResponseModel>,
                        p1: Response<UserUniversalResponseModel>
                    ) {
                        if (p1.isSuccessful)
                        {
                            progressDialog.dismiss()
                            val data = p1.body()
                            if (data!=null)
                            {
                                if (data.STS == "200")
                                {
                                    Constant.toast(this@CreateUserActivity,data.MSG)
                                    finish()
                                }
                                else
                                {
                                    Constant.toast(this@CreateUserActivity,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@CreateUserActivity,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@CreateUserActivity,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<UserUniversalResponseModel>, p1: Throwable) {
                        Constant.toast(this@CreateUserActivity,p1.message.toString())
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

    private fun callCreateFaculty(userModel: CreateUserRequestModel , departmentId : Int , token : String) {

        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()

        try {
            RetrofitInstance.getInstance().createFaculty(token, userModel , departmentId)
                .enqueue(object : Callback<UserUniversalResponseModel> {
                    override fun onResponse(
                        p0: Call<UserUniversalResponseModel>,
                        p1: Response<UserUniversalResponseModel>
                    ) {
                        if (p1.isSuccessful)
                        {
                            progressDialog.dismiss()
                            val data = p1.body()
                            if (data!=null)
                            {
                                if (data.STS == "200")
                                {
                                    Constant.toast(this@CreateUserActivity,data.MSG)
                                    finish()
                                }
                                else
                                {
                                    Constant.toast(this@CreateUserActivity,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@CreateUserActivity,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@CreateUserActivity,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<UserUniversalResponseModel>, p1: Throwable) {
                        Constant.toast(this@CreateUserActivity,p1.message.toString())
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

    private fun Valid(fullName: String, phoneNo: String, email: String, password: String, address: String): Boolean {

        if (fullName.isEmpty())
        {
            binding.etFullName.error = "Please enter the name"
            binding.etFullName.requestFocus()
            return false
        }
        if (phoneNo.isEmpty())
        {
            binding.etPhoneNo.error = "Please enter the phone number"
            binding.etPhoneNo.requestFocus()
            return false
        }
        if (email.isEmpty())
        {
            binding.etEmail.error = "Please enter the email"
            binding.etEmail.requestFocus()
            return false
        }
        if (password.isEmpty()) {
            binding.etPassword.error = "Please enter the password"
            binding.etPassword.requestFocus()
            return false
        }
        if (address.isEmpty()) {
            binding.etAddress.error = "Please enter the address"
            binding.etAddress.requestFocus()
            return false
        }
        if (ImageUrl == null)
        {
            Constant.toast(this,"Please select the profile picture")
            return false
        }
        return true
    }

    private fun callCreatePrinciple(userModel: CreateUserRequestModel , token : String) {

        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()

        try {
            RetrofitInstance.getInstance().createPrinciple(token,userModel)
                .enqueue(object : Callback<UserUniversalResponseModel> {
                    override fun onResponse(
                        p0: Call<UserUniversalResponseModel>,
                        p1: Response<UserUniversalResponseModel>
                    ) {
                        if (p1.isSuccessful)
                        {
                            progressDialog.dismiss()
                            val data = p1.body()
                            if (data!=null)
                            {
                                if (data.STS == "200")
                                {
                                    Constant.toast(this@CreateUserActivity,data.MSG)
                                    finish()
                                }
                                else
                                {
                                    Constant.toast(this@CreateUserActivity,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@CreateUserActivity,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@CreateUserActivity,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<UserUniversalResponseModel>, p1: Throwable) {
                        Constant.toast(this@CreateUserActivity,p1.message.toString())
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

    private fun callCreateSuperAdmin(userModel: CreateUserRequestModel) {

        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()

        try {
            RetrofitInstance.getInstance().createSuperAdmin(userModel)
                .enqueue(object : Callback<UserUniversalResponseModel> {
                    override fun onResponse(
                        p0: Call<UserUniversalResponseModel>,
                        p1: Response<UserUniversalResponseModel>
                    ) {
                        if (p1.isSuccessful)
                        {
                            progressDialog.dismiss()
                            val data = p1.body()
                            if (data!=null)
                            {
                                if (data.STS == "200")
                                {
                                    Constant.toast(this@CreateUserActivity,data.MSG)
                                    finish()
                                }
                                else
                                {
                                    Constant.toast(this@CreateUserActivity,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@CreateUserActivity,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@CreateUserActivity,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<UserUniversalResponseModel>, p1: Throwable) {
                        Constant.toast(this@CreateUserActivity,p1.message.toString())
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
        GetAllRoles()
        GetAllDepartments()
    }

    private fun GetAllRoles() {
        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()

        val token = SharedPreferencesManager.getString(this, SharedPreferencesManager.TOKEN)

        try {
            RetrofitInstance.getInstance().getAllRoles(token)
                .enqueue(object : Callback<GetAllRoleResponseModel> {
                    override fun onResponse(
                        p0: Call<GetAllRoleResponseModel>,
                        p1: Response<GetAllRoleResponseModel>
                    ) {
                        if (p1.isSuccessful)
                        {
                            progressDialog.dismiss()
                            val data = p1.body()
                            if (data!=null)
                            {
                                if (data.STS == "200")
                                {
                                    val roleList = data.CONTENT.filter { it.roleName != "ROLE_STUDENT" }
                                    val roleName = roleList.map { it.roleName } // Extract names

                                    // Create an ArrayAdapter for the Spinner
                                    val adapter = ArrayAdapter(
                                        this@CreateUserActivity,
                                        android.R.layout.simple_spinner_item,
                                        roleName
                                    )
                                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                                    binding.roles.adapter = adapter

                                    // Set listener to get the selected department's ID
                                    binding.roles.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                        override fun onItemSelected(
                                            parentView: AdapterView<*>,
                                            selectedItemView: View?,
                                            position: Int,
                                            id: Long
                                        ) {
                                            selectedRole= roleList[position].roleName
                                            if (selectedRole == "ROLE_FACULTY")
                                            {
                                                binding.department.visibility = View.VISIBLE
                                            }
                                            else
                                            {
                                                binding.department.visibility = View.GONE
                                            }
                                        }

                                        override fun onNothingSelected(parentView: AdapterView<*>) {

                                        }
                                    }
                                }
                                else
                                {
                                    Constant.toast(this@CreateUserActivity,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@CreateUserActivity,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@CreateUserActivity,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<GetAllRoleResponseModel>, p1: Throwable) {
                        Constant.toast(this@CreateUserActivity,p1.message.toString())
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
                                        this@CreateUserActivity,
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
                                        }

                                        override fun onNothingSelected(parentView: AdapterView<*>) {

                                        }
                                    }
                                }
                                else
                                {
                                    Constant.toast(this@CreateUserActivity,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@CreateUserActivity,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@CreateUserActivity,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<GetAllDepartmentResponseModel>, p1: Throwable) {
                        Constant.toast(this@CreateUserActivity,p1.message.toString())
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