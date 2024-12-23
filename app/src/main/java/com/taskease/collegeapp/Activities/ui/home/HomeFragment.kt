package com.taskease.collegeapp.Activities.ui.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import com.taskease.collegeapp.Activities.Faculty.Assignment.ShowAssignmentActivity
import com.taskease.collegeapp.Activities.Faculty.Attendance.ShowAttendanceActivity
import com.taskease.collegeapp.Activities.Faculty.Notes.ShowNotesActivity
import com.taskease.collegeapp.Activities.Faculty.Notice.AddNoticeActivity
import com.taskease.collegeapp.Activities.Faculty.SpreadSheet.ShowSpreadSheetActivity
import com.taskease.collegeapp.Activities.Faculty.SpreadSheet.SpreadSheetActivity
import com.taskease.collegeapp.Activities.Faculty.Workload.ShowWorkLoadActivity
import com.taskease.collegeapp.Activities.Principle.Attendance.ShowAttendanceReportActivity
import com.taskease.collegeapp.Activities.Principle.WorkLoad.ShowWorkLoadReportActivity
import com.taskease.collegeapp.Adapter.BannerAdapter
import com.taskease.collegeapp.Adapter.DashboardAdapter
import com.taskease.collegeapp.Constants.Constant
import com.taskease.collegeapp.Constants.CustomProgressDialog
import com.taskease.collegeapp.Constants.SharedPreferencesManager
import com.taskease.collegeapp.Model.DashboardItem
import com.taskease.collegeapp.Model.Response.BannerResponseModel
import com.taskease.collegeapp.R
import com.taskease.collegeapp.Retrofit.RetrofitInstance
import com.taskease.collegeapp.databinding.FragmentHomeBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var bannerRecyclerView: RecyclerView
    private val imageUrls = mutableListOf<String>()
    private val handler = Handler(Looper.getMainLooper())
    private var currentPage = 0
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        bannerRecyclerView = binding.bannerRecyclerView
        bannerRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        val role =
            SharedPreferencesManager.getString(requireContext(), SharedPreferencesManager.ROLE, "")

        if (role == "ROLE_FACULTY") {
            val list = ArrayList<DashboardItem>()
            list.add(DashboardItem(R.drawable.excel,"Add Spread Sheet", ShowSpreadSheetActivity::class.java))
            list.add(DashboardItem(R.drawable.assignment,"Add Assignment", ShowAssignmentActivity::class.java))
            list.add(DashboardItem(R.drawable.notice,"Notice", AddNoticeActivity::class.java))
            list.add(DashboardItem(R.drawable.workload,"Add WorkLoad Report", ShowWorkLoadActivity::class.java))
            list.add(DashboardItem(R.drawable.attendance,"Add Attendance", ShowAttendanceActivity::class.java))
            list.add(DashboardItem(R.drawable.note,"Add Notes", ShowNotesActivity::class.java))

            binding.recyclerView.layoutManager = GridLayoutManager(context,2, VERTICAL,false)
            binding.recyclerView.adapter = DashboardAdapter(requireContext(),list)
        }
        if (role == "ROLE_PRINCIPLE")
        {
            val list = ArrayList<DashboardItem>()
            list.add(DashboardItem(R.drawable.workload,"WorkLoad Reports", ShowWorkLoadReportActivity::class.java))
            list.add(DashboardItem(R.drawable.attendance,"Attendance", ShowAttendanceReportActivity::class.java))

            binding.recyclerView.layoutManager = GridLayoutManager(context,2, VERTICAL,false)
            binding.recyclerView.adapter = DashboardAdapter(requireContext(),list)
        }


        fetchBannerImages()

        return root
    }

    private fun fetchBannerImages() {
        val progressDialog = CustomProgressDialog(requireContext())
        progressDialog.show()

        try {
            val token = SharedPreferencesManager.getString(requireContext(), SharedPreferencesManager.TOKEN)

            RetrofitInstance.getInstance().getBanners(token).enqueue(object : Callback<BannerResponseModel> {
                override fun onResponse(
                    p0: Call<BannerResponseModel>,
                    p1: Response<BannerResponseModel>
                ) {
                    if (p1.isSuccessful) {
                        progressDialog.dismiss()
                        val data = p1.body()
                        if (data != null && data.STS == "200") {
                            data.CONTENT.let { banners ->
                                banners.forEach { banner ->
                                    imageUrls.add(banner.imageLink)
                                }
                                setupBannerAdapter()
                                startAutoSlide()
                            }
                        } else {
                            Constant.toast(requireContext(), data?.MSG ?: "Error")
                        }
                    } else {
                        Constant.toast(requireContext(), p1.message().toString())
                        progressDialog.dismiss()
                    }
                }

                override fun onFailure(p0: Call<BannerResponseModel>, p1: Throwable) {
                    Constant.toast(requireContext(), p1.message.toString())
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


    private fun setupBannerAdapter() {
        val adapter = BannerAdapter(imageUrls)
        bannerRecyclerView.adapter = adapter
    }

    private fun startAutoSlide() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (imageUrls.isNotEmpty()) {
                    currentPage = (currentPage + 1) % imageUrls.size
                    bannerRecyclerView.smoothScrollToPosition(currentPage) // Scroll to the next banner
                }
                handler.postDelayed(this, 5000) // Repeat every 5 seconds
            }
        }, 5000)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        handler.removeCallbacksAndMessages(null)
    }
}