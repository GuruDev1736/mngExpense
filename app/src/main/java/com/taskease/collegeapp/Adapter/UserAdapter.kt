package com.taskease.collegeapp.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.taskease.collegeapp.Model.Response.Users
import com.taskease.collegeapp.R
import com.taskease.collegeapp.databinding.UserLayoutBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserAdapter(val context : Context , val list : List<Users>) : RecyclerView.Adapter<UserAdapter.onViewHolder>() {

    class onViewHolder(val binding : UserLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): onViewHolder {
        val view = UserLayoutBinding.inflate(LayoutInflater.from(context),parent,false)
        return onViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: onViewHolder, position: Int) {
        holder.binding.apply {
            name.text = list[position].fullName
            email.text = list[position].email
            phoneNo.text = list[position].phoneNo
            joinDate.text = convertTimestampToDate(list[position].joinDate)
            Glide.with(context).load(list[position].profile_pic).placeholder(R.drawable.user).into(profilePic)
        }
    }

    fun convertTimestampToDate(timestamp: Long): String {
        val date = Date(timestamp * 1000)
        val format = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        return format.format(date)
    }
}