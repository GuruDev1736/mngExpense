package com.taskease.collegeapp.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.taskease.collegeapp.Model.Response.department
import com.taskease.collegeapp.Model.Response.years
import com.taskease.collegeapp.R
import com.taskease.collegeapp.databinding.SpreadSheetLayoutBinding

class DepartmentAdapter(val context : Context, val list : List<department>) : RecyclerView.Adapter<DepartmentAdapter.onViewHolder>() {

    class onViewHolder(val binding : SpreadSheetLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): onViewHolder {
        val view = SpreadSheetLayoutBinding.inflate(LayoutInflater.from(context),parent,false)
        return onViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: onViewHolder, position: Int) {
        holder.binding.apply {
            title.text = list[position].name
            imgMoreOptions.visibility = View.GONE
            imgExcelFileIcon.setImageResource(R.drawable.department)
            description.visibility = View.GONE
        }
    }
}