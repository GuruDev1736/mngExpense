package com.taskease.collegeapp.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.taskease.collegeapp.Activities.SuperAdmin.AddBatchActivity
import com.taskease.collegeapp.Constants.Constant
import com.taskease.collegeapp.Constants.CustomProgressDialog
import com.taskease.collegeapp.Constants.SharedPreferencesManager
import com.taskease.collegeapp.Model.Response.UniversalResponseModel
import com.taskease.collegeapp.Model.Response.department
import com.taskease.collegeapp.Model.Response.years
import com.taskease.collegeapp.R
import com.taskease.collegeapp.Retrofit.RetrofitInstance
import com.taskease.collegeapp.databinding.SpreadSheetLayoutBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DepartmentAdapter(val context : Context, val list : MutableList<department>) : RecyclerView.Adapter<DepartmentAdapter.onViewHolder>() {

    class onViewHolder(val binding : SpreadSheetLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): onViewHolder {
        val view = SpreadSheetLayoutBinding.inflate(LayoutInflater.from(context),parent,false)
        return onViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: onViewHolder, position: Int) {
        Constant.applySlideInAnimation(holder.itemView,position)
        holder.binding.apply {
            title.text = list[position].name
            imgMoreOptions.visibility = View.GONE
            imgExcelFileIcon.setImageResource(R.drawable.department)
            description.visibility = View.GONE
            layout.setOnClickListener{
                context.startActivity(Intent(context,AddBatchActivity::class.java)
                    .putExtra("id",list[position].id))
            }

            layout.setOnLongClickListener{
                ShowDialog(list[position].id)
                true
            }
        }
    }

    private fun ShowDialog(id: Int) {

        val builder = AlertDialog.Builder(context)

        builder.setTitle("Alert")
        builder.setMessage("Are you sure you want to delete?")

        builder.setPositiveButton("YES") { dialog, _ ->
            CallDeleteApi(id)
        }

        builder.setNegativeButton("NO") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()

    }

    private fun CallDeleteApi(id: Int) {
        val progressDialog = CustomProgressDialog(context)
        progressDialog.show()

        try {
            val token = SharedPreferencesManager.getString(context, SharedPreferencesManager.TOKEN)
            RetrofitInstance.getInstance().deleteDepartment(token,id)
                .enqueue(object : Callback<UniversalResponseModel> {
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
                                    Constant.toast(context,data.MSG)
                                    val position = list.indexOfFirst { it.id == id }
                                    if (position != -1) {
                                        list.removeAt(position)
                                        notifyItemRemoved(position)
                                        notifyItemRangeChanged(position, list.size)
                                    }
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
}