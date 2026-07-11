package com.guru.task_1.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.guru.task_1.Model.UserResponseModelItem
import com.guru.task_1.databinding.UserItemLayoutBinding


class UserAdapter(val list: ArrayList<UserResponseModelItem>) :
    RecyclerView.Adapter<UserAdapter.OnViewHolder>() {


    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): OnViewHolder {
        val binding =
            UserItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OnViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: OnViewHolder, position: Int
    ) {
        val data = list[position]
        holder.binding.apply {
            tvName.text = data.name
            tvEmail.text = data.email
            tvPhone.text = data.phone
            tvFullAddress.text =
                "${data.address.street} ${data.address.suite} ${data.address.city} ${data.address.zipcode}"

        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class OnViewHolder(val binding: UserItemLayoutBinding) : RecyclerView.ViewHolder(binding.root)
}