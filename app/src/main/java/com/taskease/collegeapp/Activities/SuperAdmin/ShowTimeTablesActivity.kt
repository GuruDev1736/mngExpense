package com.taskease.collegeapp.Activities.SuperAdmin

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.taskease.collegeapp.Activities.Faculty.Attendance.AddAttendanceActivity
import com.taskease.collegeapp.Activities.SuperAdmin.TimeTables.TimeTableViewPagerAdapter
import com.taskease.collegeapp.Constants.SharedPreferencesManager
import com.taskease.collegeapp.R
import com.taskease.collegeapp.databinding.ActivityShowTimeTablesBinding

class ShowTimeTablesActivity : AppCompatActivity() {

    private lateinit var binding : ActivityShowTimeTablesBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowTimeTablesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.actionBar.back.setOnClickListener{
            finish()
        }
        binding.actionBar.title.text = "Time Tables"

        val role = SharedPreferencesManager.getString(this@ShowTimeTablesActivity,SharedPreferencesManager.ROLE)
        if (role =="ROLE_STUDENT")
        {
            binding.createTimeTable.visibility = View.GONE
        }

        binding.createTimeTable.setOnClickListener{
            startActivity(Intent(this, CreateTimeTableActivity::class.java))
        }

        val adapter: TimeTableViewPagerAdapter = TimeTableViewPagerAdapter(this)
        binding.viewPager.setAdapter(adapter)

        TabLayoutMediator(
            binding.tabLayout, binding.viewPager
        ) { tab: TabLayout.Tab, position: Int ->
            if (position == 0) {
                tab.setText("Exam")
            } else {
                tab.setText("Regular")
            }
        }.attach()
    }
}