package com.taskease.collegeapp.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.taskease.collegeapp.Model.Response.spreadSheet
import com.taskease.collegeapp.databinding.SpreadSheetLayoutBinding

class SpreadSheetAdapter(private val context : Context , private val list : List<spreadSheet>) :
    RecyclerView.Adapter<SpreadSheetAdapter.onViewHolder>() {

    class onViewHolder(val binding: SpreadSheetLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): onViewHolder {
        val view  = SpreadSheetLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return onViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: onViewHolder, position: Int) {
        val data = list[position]
        holder.binding.apply {
            title.text = data.title
            description.text = data.description
        }
    }
}