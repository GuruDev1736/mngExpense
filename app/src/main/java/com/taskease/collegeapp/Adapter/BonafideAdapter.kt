package com.taskease.collegeapp.Adapter

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.taskease.collegeapp.Activities.OpenSpreadSheetActivity
import com.taskease.collegeapp.Activities.SuperAdmin.AddBatchActivity
import com.taskease.collegeapp.Constants.Constant
import com.taskease.collegeapp.Constants.CustomProgressDialog
import com.taskease.collegeapp.Constants.SharedPreferencesManager
import com.taskease.collegeapp.Model.Response.Bonafide
import com.taskease.collegeapp.Model.Response.UniversalResponseModel
import com.taskease.collegeapp.Model.Response.batches
import com.taskease.collegeapp.Model.Response.department
import com.taskease.collegeapp.Model.Response.years
import com.taskease.collegeapp.R
import com.taskease.collegeapp.Retrofit.RetrofitInstance
import com.taskease.collegeapp.databinding.BonfideLayoutBinding
import com.taskease.collegeapp.databinding.SpreadSheetLayoutBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BonafideAdapter(val context : Context, val list : MutableList<Bonafide>) : RecyclerView.Adapter<BonafideAdapter.onViewHolder>() {

    class onViewHolder(val binding : BonfideLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): onViewHolder {
        val view = BonfideLayoutBinding.inflate(LayoutInflater.from(context),parent,false)
        return onViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: onViewHolder, position: Int) {
        Constant.applySlideInAnimation(holder.itemView,position)
        val data = list[position]
        holder.binding.apply {
            imgExcelFileIcon.setImageResource(R.drawable.bonafide)
            title.text = data.title
            description.text = data.description

            val role = SharedPreferencesManager.getString(context,SharedPreferencesManager.ROLE)

            if (role == "ROLE_STUDENT")
            {
                user.visibility =  View.GONE
                delete.visibility = View.GONE
            }

            user.setOnClickListener{
                showCustomDialog(data)
            }

            layout.setOnClickListener{
                context.startActivity(Intent(context,OpenSpreadSheetActivity::class.java)
                    .putExtra("title",data.title)
                    .putExtra("description",data.description)
                    .putExtra("code","bonafide")
                )
            }

            delete.setOnClickListener{
                callDeleteApi(data.id)
            }
        }
    }

    private fun callDeleteApi(id: Int) {
        val token = SharedPreferencesManager.getString(context, SharedPreferencesManager.TOKEN)
        val progressDialog = CustomProgressDialog(context)
        progressDialog.show()

        try {
            RetrofitInstance.getInstance().deleteBonafide(token,id)
                .enqueue(object : Callback<UniversalResponseModel>{
                    override fun onResponse(
                        p0: Call<UniversalResponseModel>,
                        p1: Response<UniversalResponseModel>
                    ) {
                        if (p1.isSuccessful)
                        {
                            progressDialog.dismiss()
                            val data = p1.body()
                            if (data!=null)
                            {
                                if (data.STS == "200")
                                {
                                    val position = list.indexOfFirst { it.id == id }
                                    if (position != -1) {
                                        list.removeAt(position)
                                        notifyItemRemoved(position)
                                        notifyItemRangeChanged(position, list.size)
                                    }
                                    Constant.toast(context,data.MSG)
                                }
                                else
                                {
                                    Constant.toast(context,data.MSG)
                                }
                            }
                            else{
                                Constant.toast(context,p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(context,p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<UniversalResponseModel>, p1: Throwable) {
                        Constant.toast(context,p1.message.toString())
                        progressDialog.dismiss()
                    }
                })
        }catch (e : Exception)
        {
            Constant.toast(context,"Something went wrong")
            e.printStackTrace()
            progressDialog.dismiss()
        }
    }

    private fun showCustomDialog(data : Bonafide) {

        val dialogView = LayoutInflater.from(context).inflate(R.layout.student_details_dialog, null)
        val dialog = Dialog(context)
        dialog.setContentView(dialogView)

        val name = dialogView.findViewById<TextView>(R.id.name)
        val email = dialogView.findViewById<TextView>(R.id.email)
        val phone = dialogView.findViewById<TextView>(R.id.phone)
        val enrollmentNo = dialogView.findViewById<TextView>(R.id.enroll)
        val year = dialogView.findViewById<TextView>(R.id.year)
        val department = dialogView.findViewById<TextView>(R.id.department)
        val batch = dialogView.findViewById<TextView>(R.id.batch)


        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        name.text = data.student.fullName
        email.text = data.student.email
        phone.text = data.student.phone
        enrollmentNo.text = data.student.enrollment
        year.text = data.student.year.year
        department.text = data.student.department.name
        batch.text = data.student.batch.batchName
        Glide.with(context).load(data.student.profile_pic).into(dialogView.findViewById(R.id.profile_pic))

        dialog.show()
    }
}