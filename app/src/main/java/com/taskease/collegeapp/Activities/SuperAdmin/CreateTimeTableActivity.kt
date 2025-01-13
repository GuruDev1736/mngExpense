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
import com.google.firebase.storage.FirebaseStorage
import com.taskease.collegeapp.Constants.Constant
import com.taskease.collegeapp.Constants.CustomProgressDialog
import com.taskease.collegeapp.Constants.SharedPreferencesManager
import com.taskease.collegeapp.Model.Request.CreateSpreadSheetRequestModel
import com.taskease.collegeapp.Model.Request.CreateTimeTableRequestModel
import com.taskease.collegeapp.Model.Response.CreateTimeTableResponseModel
import com.taskease.collegeapp.Model.Response.GetAllBatchResponseModel
import com.taskease.collegeapp.Model.Response.GetAllDepartmentResponseModel
import com.taskease.collegeapp.Model.Response.GetAllYearResponseModel
import com.taskease.collegeapp.Model.Response.SingleSpreadSheetResponseModel
import com.taskease.collegeapp.R
import com.taskease.collegeapp.Retrofit.RetrofitInstance
import com.taskease.collegeapp.databinding.ActivityCreateTimeTableBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreateTimeTableActivity : AppCompatActivity() {

    private lateinit var binding : ActivityCreateTimeTableBinding
    private lateinit var storage : FirebaseStorage
    private var fileUrl : String? = null
    private var departmentId: Int = -1
    private var yearId: Int = -1
    private var batchId: Int = -1

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

        storage.getReference("TimeTable").child(System.currentTimeMillis().toString())
            .putFile(uri).addOnSuccessListener { it->
                it.storage.downloadUrl.addOnSuccessListener {
                    progressDialog.dismiss()
                    fileUrl = it.toString()
                    binding.linkInput.setText(fileUrl)
                    Log.d("FILE_URL",fileUrl.toString())
                }.addOnFailureListener{
                    Constant.toast(this@CreateTimeTableActivity,it.message.toString())
                    progressDialog.dismiss()
                }
            }.addOnFailureListener{
                Constant.toast(this@CreateTimeTableActivity,it.message.toString())
                progressDialog.dismiss()
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateTimeTableBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storage = FirebaseStorage.getInstance()
        binding.actionBar.back.setOnClickListener{
            finish()
        }
        binding.actionBar.title.text = "Create Time Table"


        binding.upload.setOnClickListener{
            filePickerLauncher.launch("*/*")
        }

        val adapter = ArrayAdapter.createFromResource(this@CreateTimeTableActivity,R.array.timeTable_Category,android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.category.adapter = adapter


        binding.submit.setOnClickListener{

            val title = binding.titleInput.text.toString()
            val description = binding.descriptionInput.text.toString()
            val link = binding.linkInput.text.toString()
            val category = binding.category.selectedItem.toString()

            if (Valid(title,description,link))
            {
                CallCreateTimeTableApi(title,description,link,category)
            }
        }


        val code = intent.getStringExtra("code").toString()
        val title = intent.getStringExtra("title").toString()
        val description = intent.getStringExtra("description").toString()
        val link = intent.getStringExtra("link").toString()
        val category = intent.getStringExtra("category").toString()

        if (code.equals("update"))
        {
            binding.actionBar.title.text = "Update Time Table"
            binding.submit.text = "Update"
            binding.titleInput.setText(title)
            binding.descriptionInput.setText(description)
            binding.linkInput.setText(link)
            binding.year.visibility = View.GONE
            binding.department.visibility = View.GONE
            binding.batch.visibility = View.GONE
            if (category.equals("EXAM" , ignoreCase = true))
            {
                binding.category.setSelection(0)
            }
            else if (category.equals("REGULAR" , ignoreCase = true))
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
                    CallUpdateAPI(title,description,link,category)
                }
            }
        }

    }

    private fun CallUpdateAPI(title: String, description: String, link: String, category: String) {
        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()

        try {
            val token = SharedPreferencesManager.getString(this, SharedPreferencesManager.TOKEN)
            val id = intent.getIntExtra("id",-1)

            val model = CreateTimeTableRequestModel(category,description,link,title)
            RetrofitInstance.getInstance().updateTimeTable(token,id,model)
                .enqueue(object : Callback<CreateTimeTableResponseModel> {
                    override fun onResponse(
                        p0: Call<CreateTimeTableResponseModel>,
                        p1: Response<CreateTimeTableResponseModel>
                    ) {
                        if (p1.isSuccessful)
                        {
                            progressDialog.dismiss()
                            val data = p1.body()
                            if (data!=null)
                            {
                                if (data.STS == "200")
                                {
                                    Constant.toast(this@CreateTimeTableActivity,data.MSG)
                                    finish()
                                }
                                else
                                {
                                    Constant.toast(this@CreateTimeTableActivity,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@CreateTimeTableActivity,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@CreateTimeTableActivity,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<CreateTimeTableResponseModel>, p1: Throwable) {
                        Constant.toast(this@CreateTimeTableActivity,p1.message.toString())
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
        GetAllYears()
        GetAllDepartments()
    }

    private fun CallCreateTimeTableApi(title: String, description: String, link: String, category: String) {

        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()

        try {
            val token = SharedPreferencesManager.getString(this, SharedPreferencesManager.TOKEN)
            val userId = SharedPreferencesManager.getInt(this, SharedPreferencesManager.USERID)
            val model = CreateTimeTableRequestModel(category,description,link,title)
            RetrofitInstance.getInstance().createTimeTable(token,model,yearId,departmentId,batchId)
                .enqueue(object : Callback<CreateTimeTableResponseModel> {
                    override fun onResponse(
                        p0: Call<CreateTimeTableResponseModel>,
                        p1: Response<CreateTimeTableResponseModel>
                    ) {
                        if (p1.isSuccessful)
                        {
                            progressDialog.dismiss()
                            val data = p1.body()
                            if (data!=null)
                            {
                                if (data.STS == "200")
                                {
                                    Constant.toast(this@CreateTimeTableActivity,data.MSG)
                                    finish()
                                }
                                else
                                {
                                    Constant.toast(this@CreateTimeTableActivity,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@CreateTimeTableActivity,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@CreateTimeTableActivity,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<CreateTimeTableResponseModel>, p1: Throwable) {
                        Constant.toast(this@CreateTimeTableActivity,p1.message.toString())
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
                                        this@CreateTimeTableActivity,
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
                                    Constant.toast(this@CreateTimeTableActivity,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@CreateTimeTableActivity,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@CreateTimeTableActivity,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<GetAllYearResponseModel>, p1: Throwable) {
                        Constant.toast(this@CreateTimeTableActivity,p1.message.toString())
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
                                        this@CreateTimeTableActivity,
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
                                    Constant.toast(this@CreateTimeTableActivity,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@CreateTimeTableActivity,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@CreateTimeTableActivity,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<GetAllDepartmentResponseModel>, p1: Throwable) {
                        Constant.toast(this@CreateTimeTableActivity,p1.message.toString())
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
                                        this@CreateTimeTableActivity,
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
                                    Constant.toast(this@CreateTimeTableActivity,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@CreateTimeTableActivity,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@CreateTimeTableActivity,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<GetAllBatchResponseModel>, p1: Throwable) {
                        Constant.toast(this@CreateTimeTableActivity,p1.message.toString())
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