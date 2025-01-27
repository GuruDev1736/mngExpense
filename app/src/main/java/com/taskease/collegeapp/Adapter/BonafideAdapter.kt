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
import com.taskease.collegeapp.Model.Response.Bonafide
import com.taskease.collegeapp.Model.Response.UniversalResponseModel
import com.taskease.collegeapp.Model.Response.batches
import com.taskease.collegeapp.Model.Response.department
import com.taskease.collegeapp.Model.Response.years
import com.taskease.collegeapp.R
import com.taskease.collegeapp.Retrofit.RetrofitInstance
import com.taskease.collegeapp.databinding.SpreadSheetLayoutBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BonafideAdapter(val context : Context, val list : MutableList<Bonafide>) : RecyclerView.Adapter<BonafideAdapter.onViewHolder>() {

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
        val data = list[position]
        holder.binding.apply {
            imgExcelFileIcon.setImageResource(R.drawable.bonafide)
            title.text = data.title
            description.text = data.description
        }
    }
}