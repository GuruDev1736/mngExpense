package com.taskease.collegeapp.Activities.Faculty.Attendance

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.taskease.collegeapp.Activities.Faculty.Workload.AddWorkLoadReport
import com.taskease.collegeapp.Adapter.SpreadSheetAdapter
import com.taskease.collegeapp.Constants.Constant
import com.taskease.collegeapp.Constants.CustomProgressDialog
import com.taskease.collegeapp.Constants.SharedPreferencesManager
import com.taskease.collegeapp.Model.Response.SpreadSheetResponseModel
import com.taskease.collegeapp.R
import com.taskease.collegeapp.Retrofit.RetrofitInstance
import com.taskease.collegeapp.databinding.ActivityShowAttendanceBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ShowAttendanceActivity : AppCompatActivity() {

    private lateinit var binding : ActivityShowAttendanceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.actionBar.back.setOnClickListener{
            finish()
        }
        binding.actionBar.title.text = "Attendance"
        binding.addSpreadSheet.setOnClickListener{
            startActivity(Intent(this, AddAttendanceActivity::class.java))
        }

        val adapter: MyFragmentAdapter = MyFragmentAdapter(this)
        binding.viewPager.setAdapter(adapter)

        TabLayoutMediator(
            binding.tabLayout, binding.viewPager
        ) { tab: TabLayout.Tab, position: Int ->
            if (position == 0) {
                tab.setText("Teacher")
            } else {
                tab.setText("Student")
            }
        }.attach()
    }
}