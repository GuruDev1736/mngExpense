package com.taskease.collegeapp.Activities.Faculty.Attendance

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.taskease.collegeapp.Adapter.SpreadSheetAdapter
import com.taskease.collegeapp.Constants.Constant
import com.taskease.collegeapp.Constants.CustomProgressDialog
import com.taskease.collegeapp.Constants.SharedPreferencesManager
import com.taskease.collegeapp.Model.Response.SpreadSheetResponseModel
import com.taskease.collegeapp.Retrofit.RetrofitInstance
import com.taskease.collegeapp.databinding.FragmentTeacherBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class TeacherFragment : Fragment() {

    private lateinit var binding : FragmentTeacherBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentTeacherBinding.inflate(layoutInflater)
        val root = binding.root

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        return root
    }

    override fun onStart() {
        super.onStart()
        CallGetAllAttendanceReport()
    }

    private fun CallGetAllAttendanceReport() {
        val progressDialog = CustomProgressDialog(requireContext())
        progressDialog.show()

        try {
            val token = SharedPreferencesManager.getString(requireContext(), SharedPreferencesManager.TOKEN)
            val departmentId = SharedPreferencesManager.getInt(requireContext(), SharedPreferencesManager.DEPARTMENTID)
            val userId = SharedPreferencesManager.getInt(requireContext(), SharedPreferencesManager.USERID)

            RetrofitInstance.getInstance().getAllSpreadSheetByUser(token,"TEACHER",departmentId,userId)
                .enqueue(object : Callback<SpreadSheetResponseModel> {
                    override fun onResponse(
                        p0: Call<SpreadSheetResponseModel>,
                        p1: Response<SpreadSheetResponseModel>
                    ) {
                        if (p1.isSuccessful)
                        {
                            progressDialog.dismiss()
                            val data = p1.body()
                            if (data!=null)
                            {
                                if (data.STS == "200")
                                {
                                    if (data.CONTENT.isEmpty())
                                    {
                                        Constant.toast(requireContext(),"No Data Found")
                                        binding.noData.visibility = View.VISIBLE
                                    }
                                    else
                                    {
                                        binding.noData.visibility = View.GONE
                                    }
                                    val adapter = SpreadSheetAdapter(requireContext(),data.CONTENT.toMutableList(),"TEACHER")
                                    binding.recyclerView.adapter = adapter
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

                    override fun onFailure(p0: Call<SpreadSheetResponseModel>, p1: Throwable) {
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
