package com.taskease.collegeapp.Adapter

import android.animation.ObjectAnimator
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.core.animation.addListener
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.taskease.collegeapp.Constants.Constant
import com.taskease.collegeapp.Constants.CustomProgressDialog
import com.taskease.collegeapp.Constants.SharedPreferencesManager
import com.taskease.collegeapp.Model.Request.CreateLostReportRequestModel
import com.taskease.collegeapp.Model.Response.CreateLostReportResponseModel
import com.taskease.collegeapp.Model.Response.Report
import com.taskease.collegeapp.Model.Response.UniversalResponseModel
import com.taskease.collegeapp.R
import com.taskease.collegeapp.Retrofit.RetrofitInstance
import com.taskease.collegeapp.databinding.ReportLayoutBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReportAdapter(val context: Context, val list: MutableList<Report> , val code : String) :
    RecyclerView.Adapter<ReportAdapter.ViewHolder>() {

    private var expandedPosition = -1

    class ViewHolder(val binding: ReportLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ReportLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Constant.applySlideInAnimation(holder.itemView, position)

        val data = list[position]
        holder.binding.apply {
            val isExpanded = position == expandedPosition
            animateExpansion(expandableLayout, isExpanded)


            title.text = data.title
            fullDescriptionTextView.text = data.description
            Glide.with(context).load(data.link).placeholder(R.drawable.image_not_found).into(image)
            owner.text = data.student.fullName
            year.text = data.student.year.year
            department.text = data.student.department.name
            batch.text = data.student.batch.batchName

            when {
                data.status.equals("LOST", ignoreCase = true) -> {
                    status.setTextColor(Color.RED)
                }
                data.status.equals("FOUND", ignoreCase = true) -> {
                    status.setTextColor(Color.GREEN)
                }
            }
            status.text = data.status

            // Handle click to expand/collapse
            layout.setOnClickListener {
                val previousExpandedPosition = expandedPosition
                expandedPosition = if (isExpanded) -1 else position
                notifyItemChanged(previousExpandedPosition)
                notifyItemChanged(expandedPosition)
            }


            if(code.equals("HISTORY"))
            {
                buttonLayout.visibility = View.VISIBLE
                if (data.status.equals("FOUND", ignoreCase = true))
                {
                    update.visibility = View.GONE
                }
                else
                {
                    update.visibility = View.VISIBLE
                }
                delete.setOnClickListener{
                    CallDeleteApi(data.id)
                }

                update.setOnClickListener{
                    showCustomDialog(data)
                }
            }
        }
    }


    private fun showCustomDialog(data : Report) {

        val dialogView = LayoutInflater.from(context).inflate(R.layout.add_lost_report_dailog, null)
        val dialog = Dialog(context)
        dialog.setContentView(dialogView)

        val title = dialogView.findViewById<EditText>(R.id.title)
        val description = dialogView.findViewById<EditText>(R.id.description)
        val status = dialogView.findViewById<Spinner>(R.id.status)
        val linkLayout = dialogView.findViewById<LinearLayout>(R.id.link_layout)
        val submit = dialogView.findViewById<Button>(R.id.submit)
        val close = dialogView.findViewById<Button>(R.id.close)

        title.setText(data.title)
        description.setText(data.description)

        linkLayout.visibility = View.GONE
        status.visibility = View.VISIBLE

        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val adapter = ArrayAdapter.createFromResource(context,R.array.status,android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        status.adapter = adapter

        submit.setOnClickListener {
            val title = title.text.toString()
            val descrip = description.text.toString()
            val itemStatus = status.selectedItem.toString()
            if (title.isEmpty() || descrip.isEmpty()) {
                Constant.toast(context, "Please Enter All the details")
            } else {
                CallUpdateReportApi(title , descrip, dialog , itemStatus,data)
            }
        }

        close.setOnClickListener{
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun CallUpdateReportApi(title: String, descrip: String, dialog: Dialog, itemStatus: String, item: Report) {
        val token = SharedPreferencesManager.getString(context, SharedPreferencesManager.TOKEN)
        val progressDialog = CustomProgressDialog(context)
        progressDialog.show()

        try {
            val model = CreateLostReportRequestModel(descrip,item.link,itemStatus,title)
            RetrofitInstance.getInstance().updateReport(token,item.id,model)
                .enqueue(object : Callback<CreateLostReportResponseModel> {
                    override fun onResponse(
                        p0: Call<CreateLostReportResponseModel>,
                        p1: Response<CreateLostReportResponseModel>
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
                                    dialog.dismiss()
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

                    override fun onFailure(p0: Call<CreateLostReportResponseModel>, p1: Throwable) {
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


    private fun CallDeleteApi(id: Int) {
        val token = SharedPreferencesManager.getString(context, SharedPreferencesManager.TOKEN)
        val progressDialog = CustomProgressDialog(context)
        progressDialog.show()
        try {
            RetrofitInstance.getInstance().deleteReport(token,id)
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


    private fun truncateText(text: String, maxLength: Int): String {
        return if (text.length > maxLength) {
            "${text.substring(0, maxLength)}..."
        } else {
            text
        }
    }

    private fun animateExpansion(view: View, isExpanding: Boolean) {
        val initialHeight = if (isExpanding) 0 else view.measuredHeight
        val targetHeight = if (isExpanding) view.measuredHeight else 0

        if (isExpanding) {
            view.visibility = View.VISIBLE
        }

        val animator = ObjectAnimator.ofInt(view, "height", initialHeight, targetHeight).apply {
            duration = 300
            interpolator = OvershootInterpolator()
            addListener(onEnd = {
                if (!isExpanding) view.visibility = View.GONE
            })
        }
        animator.start()
    }
}