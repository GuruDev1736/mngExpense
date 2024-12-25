package com.taskease.collegeapp.Activities.Principle.Deadlines

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.datepicker.MaterialDatePicker
import com.taskease.collegeapp.Constants.Constant
import com.taskease.collegeapp.Constants.CustomProgressDialog
import com.taskease.collegeapp.Constants.SharedPreferencesManager
import com.taskease.collegeapp.Model.Request.CreateDeadlineRequestModel
import com.taskease.collegeapp.Model.Request.CreateSpreadSheetRequestModel
import com.taskease.collegeapp.Model.Response.CreateDeadlineResponseModel
import com.taskease.collegeapp.Model.Response.SingleSpreadSheetResponseModel
import com.taskease.collegeapp.R
import com.taskease.collegeapp.Retrofit.RetrofitInstance
import com.taskease.collegeapp.databinding.ActivityAddDeadlineBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddDeadlineActivity : AppCompatActivity() {

    private lateinit var binding : ActivityAddDeadlineBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddDeadlineBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.actionBar.back.setOnClickListener{
            finish()
        }

        binding.actionBar.title.text = "Add Deadline"

        binding.startDatebtn.setOnClickListener {
            showMaterialDatePicker(binding.startDate)
        }

        binding.endDatebtn.setOnClickListener{
            showMaterialDatePicker(binding.endDate)
        }

        binding.submit.setOnClickListener{
            val title = binding.titleInput.text.toString()
            val description = binding.descriptionInput.text.toString()
            val startDate = binding.startDate.text.toString()
            val endDate = binding.endDate.text.toString()

            if (Valid(title,description,startDate,endDate)) {
                CallCreateDeadlineApi(title,description,startDate,endDate)
            }
        }

        val code = intent.getStringExtra("code")
        val id = intent.getIntExtra("id",0)

        if (code.equals("update"))
        {
            binding.actionBar.title.text = "Update Deadline"
            binding.submit.text = "Update"
            binding.titleInput.setText(intent.getStringExtra("title"))
            binding.descriptionInput.setText(intent.getStringExtra("description"))
            binding.startDate.setText(intent.getStringExtra("startDate"))
            binding.endDate.setText(intent.getStringExtra("endDate"))

            binding.submit.setOnClickListener{
                val title = binding.titleInput.text.toString()
                val description = binding.descriptionInput.text.toString()
                val startDate = binding.startDate.text.toString()
                val endDate = binding.endDate.text.toString()

                if (Valid(title,description,startDate,endDate)) {
                    CallUpdateDeadlineApi(title,description,startDate,endDate,id)
                }
            }

        }

        if (code.equals("show"))
        {
            binding.actionBar.title.text = "Show Deadline"
            binding.submit.visibility = View.GONE
            binding.titleInput.setText(intent.getStringExtra("title"))
            binding.descriptionInput.setText(intent.getStringExtra("description"))
            binding.startDate.setText(intent.getStringExtra("startDate"))
            binding.endDate.setText(intent.getStringExtra("endDate"))

            binding.titleInput.isEnabled = false
            binding.descriptionInput.isEnabled = false
            binding.startDate.isEnabled = false
            binding.endDate.isEnabled = false
            binding.startDatebtn.isEnabled = false
            binding.endDatebtn.isEnabled = false

        }

    }

    private fun CallCreateDeadlineApi(title: String, description: String, startDate: String, endDate: String) {

        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()

        try {
            val token = SharedPreferencesManager.getString(this, SharedPreferencesManager.TOKEN)
            val userId = SharedPreferencesManager.getInt(this, SharedPreferencesManager.USERID)
            val model = CreateDeadlineRequestModel(description,endDate,startDate, title)
            RetrofitInstance.getInstance().createDeadline(token,userId,model)
                .enqueue(object : Callback<CreateDeadlineResponseModel> {
                    override fun onResponse(
                        p0: Call<CreateDeadlineResponseModel>,
                        p1: Response<CreateDeadlineResponseModel>
                    ) {
                        if (p1.isSuccessful)
                        {
                            progressDialog.dismiss()
                            val data = p1.body()
                            if (data!=null)
                            {
                                if (data.STS == "200")
                                {
                                    Constant.toast(this@AddDeadlineActivity,data.MSG)
                                    finish()
                                }
                                else
                                {
                                    Constant.toast(this@AddDeadlineActivity,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@AddDeadlineActivity,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@AddDeadlineActivity,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<CreateDeadlineResponseModel>, p1: Throwable) {
                        Constant.toast(this@AddDeadlineActivity,p1.message.toString())
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


    private fun CallUpdateDeadlineApi(title: String, description: String, startDate: String, endDate: String , id : Int) {
        val progressDialog = CustomProgressDialog(this)
        progressDialog.show()
        try {
            val token = SharedPreferencesManager.getString(this, SharedPreferencesManager.TOKEN)
            val model = CreateDeadlineRequestModel(description,endDate,startDate, title)
            RetrofitInstance.getInstance().updateDeadline(token,id,model)
                .enqueue(object : Callback<CreateDeadlineResponseModel> {
                    override fun onResponse(
                        p0: Call<CreateDeadlineResponseModel>,
                        p1: Response<CreateDeadlineResponseModel>
                    ) {
                        if (p1.isSuccessful)
                        {
                            progressDialog.dismiss()
                            val data = p1.body()
                            if (data!=null)
                            {
                                if (data.STS == "200")
                                {
                                    Constant.toast(this@AddDeadlineActivity,data.MSG)
                                    finish()
                                }
                                else
                                {
                                    Constant.toast(this@AddDeadlineActivity,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(this@AddDeadlineActivity,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(this@AddDeadlineActivity,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<CreateDeadlineResponseModel>, p1: Throwable) {
                        Constant.toast(this@AddDeadlineActivity,p1.message.toString())
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

    private fun Valid(title: String, description: String, startDate: String, endDate: String): Boolean {

        if (title.isEmpty())
        {
            binding.titleInput.error = "Title is required"
            binding.titleInput.requestFocus()
            return false
        }
        if (description.isEmpty()) {
            binding.descriptionInput.error = "Description is required"
            binding.descriptionInput.requestFocus()
            return false
        }
        if (startDate.isEmpty()) {
            binding.startDate.error = "Start Date is required"
            binding.startDate.requestFocus()
            return false
        }
        if (endDate.isEmpty()) {
            binding.endDate.error = "End Date is required"
            binding.endDate.requestFocus()
            return false
        }
        return true
    }

    private fun showMaterialDatePicker(editText: EditText) {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select a date")
            .build()

        datePicker.show(supportFragmentManager, "MATERIAL_DATE_PICKER")
        datePicker.addOnPositiveButtonClickListener { selection ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = selection
            val formattedDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(calendar.time)
            editText.setText(formattedDate)
        }
    }
}