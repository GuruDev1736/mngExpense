package com.taskease.collegeapp.Adapter


import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.android.material.badge.ExperimentalBadgeUtils
import com.taskease.collegeapp.Model.DashboardItem
import com.taskease.collegeapp.databinding.DashboardItemBinding

class DashboardAdapter(val context: Context,val list: List<DashboardItem>) : RecyclerView.Adapter<DashboardAdapter.onViewHolder>() {

    class onViewHolder(val binding: DashboardItemBinding) : RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): onViewHolder {
        val view = DashboardItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return onViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @OptIn(ExperimentalBadgeUtils::class)
    override fun onBindViewHolder(holder: onViewHolder, position: Int) {

        val data = list[position]
        holder.binding.apply {
            image.setImageResource(data.image)
            title.text = data.title
            layout.setOnClickListener{
                context.startActivity(Intent(context, data.activityClass))
            }
        }
    }
}