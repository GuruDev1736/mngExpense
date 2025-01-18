package com.taskease.collegeapp.Adapter

import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.taskease.collegeapp.Activities.Co_operative.AddStockActivity
import com.taskease.collegeapp.Activities.Faculty.Assignment.AddAssignmentActivity
import com.taskease.collegeapp.Activities.Faculty.Attendance.AddAttendanceActivity
import com.taskease.collegeapp.Activities.Faculty.Notes.AddNotesActivity
import com.taskease.collegeapp.Activities.Faculty.SpreadSheet.SpreadSheetActivity
import com.taskease.collegeapp.Activities.Faculty.Workload.AddWorkLoadReport
import com.taskease.collegeapp.Activities.Lib.AddBooksActivity
import com.taskease.collegeapp.Activities.Office.AddBudgetActivity
import com.taskease.collegeapp.Activities.OpenSpreadSheetActivity
import com.taskease.collegeapp.Constants.Constant
import com.taskease.collegeapp.Constants.CustomProgressDialog
import com.taskease.collegeapp.Constants.SharedPreferencesManager
import com.taskease.collegeapp.Model.Response.BookDetails
import com.taskease.collegeapp.Model.Response.Books
import com.taskease.collegeapp.Model.Response.Stocks
import com.taskease.collegeapp.Model.Response.UniversalResponseModel
import com.taskease.collegeapp.Model.Response.spreadSheet
import com.taskease.collegeapp.R
import com.taskease.collegeapp.Retrofit.RetrofitInstance
import com.taskease.collegeapp.databinding.SpreadSheetLayoutBinding
import com.taskease.collegeapp.databinding.StockLayoutBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StocksAdapter(private val context : Context, private val list : MutableList<Stocks>) :
    RecyclerView.Adapter<StocksAdapter.onViewHolder>() {

    class onViewHolder(val binding: StockLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): onViewHolder {
        val view  = StockLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return onViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: onViewHolder, position: Int) {
        Constant.applySlideInAnimation(holder.itemView,position)
        val data = list[position]
        holder.binding.apply {
            title.text = truncateText(data.title, 30)
            description.text = truncateText(data.description, 30)
            price.text = data.price

            delete.setOnClickListener {
                CallDeleteApi(data.id)
            }

            if (data.status)
            {
                status.text = "Avaliable"
                toggle.isChecked = true
            }
            else
            {
                status.text = "Not Avaliable"
                toggle.isChecked = false
            }


            toggle.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    calToggleApi(data.id)
                } else {
                    calToggleApi(data.id)
                }
            }
        }
    }

    private fun calToggleApi(stockId : Int) {

        val progressDialog = CustomProgressDialog(context)
        progressDialog.show()

        try {
            val token = SharedPreferencesManager.getString(context, SharedPreferencesManager.TOKEN)

            RetrofitInstance.getInstance().updateStockStatus(token,stockId).enqueue(object : Callback<UniversalResponseModel> {
                override fun onResponse(
                    p0: Call<UniversalResponseModel>,
                    p1: Response<UniversalResponseModel>
                ) {
                    if (p1.isSuccessful) {
                        progressDialog.dismiss()
                        val data = p1.body()
                        if (data != null && data.STS == "200") {
                            Constant.toast(context,data.CONTENT)
                        } else {
                            Constant.toast(context, data?.MSG ?: "Error")
                        }
                    } else {
                        Constant.toast(context, p1.message().toString())
                        progressDialog.dismiss()
                    }
                }

                override fun onFailure(p0: Call<UniversalResponseModel>, p1: Throwable) {
                    Constant.toast(context, p1.message.toString())
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


    private fun CallDeleteApi(id: Int) {
        val token = SharedPreferencesManager.getString(context, SharedPreferencesManager.TOKEN)
        val progressDialog = CustomProgressDialog(context)
        progressDialog.show()
        try {
            RetrofitInstance.getInstance().deleteStock(token,id)
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

    fun truncateText(text: String, maxLength: Int): String {
        return if (text.length > maxLength) {
            "${text.substring(0, maxLength)}..."
        } else {
            text
        }
    }

}