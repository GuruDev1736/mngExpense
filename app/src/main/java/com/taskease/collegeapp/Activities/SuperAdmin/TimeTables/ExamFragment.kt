package com.taskease.collegeapp.Activities.SuperAdmin.TimeTables

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.taskease.collegeapp.Adapter.SpreadSheetAdapter
import com.taskease.collegeapp.Adapter.StoreAdapter
import com.taskease.collegeapp.Adapter.TimeTableAdapter
import com.taskease.collegeapp.Constants.Constant
import com.taskease.collegeapp.Constants.CustomProgressDialog
import com.taskease.collegeapp.Constants.SharedPreferencesManager
import com.taskease.collegeapp.Model.Response.GetAllStoreResponseModel
import com.taskease.collegeapp.Model.Response.TimeTableResponseModel
import com.taskease.collegeapp.R
import com.taskease.collegeapp.Retrofit.RetrofitInstance
import com.taskease.collegeapp.databinding.FragmentExamBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ExamFragment : Fragment() {

    private lateinit var binding : FragmentExamBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
      binding = FragmentExamBinding.inflate(layoutInflater)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        CallAllExamTimetables()
    }

    private fun CallAllExamTimetables() {
        val progressDialog = CustomProgressDialog(requireContext())
        progressDialog.show()

        val token = SharedPreferencesManager.getString(requireContext(), SharedPreferencesManager.TOKEN)

        try {
            RetrofitInstance.getInstance().getAllTimeTableByCategory(token,"EXAM")
                .enqueue(object : Callback<TimeTableResponseModel> {
                    override fun onResponse(
                        p0: Call<TimeTableResponseModel>,
                        p1: Response<TimeTableResponseModel>
                    ) {
                        if (p1.isSuccessful)
                        {
                            progressDialog.dismiss()
                            val data = p1.body()
                            if (data!=null)
                            {
                                if (data.STS == "200")
                                {
                                    binding.recyclerView.adapter = TimeTableAdapter(requireContext(),data.CONTENT.toMutableList())
                                }
                                else
                                {
                                    Constant.toast(requireContext(),data.MSG)
                                }
                            }
                            else{
                                Constant.toast(requireContext(),p1.message().toString())
                                progressDialog.dismiss()
                            }
                        }
                        else{
                            Constant.toast(requireContext(),p1.message().toString())
                            progressDialog.dismiss()
                        }
                    }

                    override fun onFailure(p0: Call<TimeTableResponseModel>, p1: Throwable) {
                        Constant.toast(requireContext(),p1.message.toString())
                        progressDialog.dismiss()
                    }

                })
        }catch (e : Exception)
        {
            Constant.toast(requireContext(),"Something went wrong")
            e.printStackTrace()
            progressDialog.dismiss()
        }
    }
}