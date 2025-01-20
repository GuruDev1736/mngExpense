package com.taskease.collegeapp.Activities.Student.Hostel

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import com.taskease.collegeapp.Activities.Faculty.Assignment.ShowAssignmentActivity
import com.taskease.collegeapp.Activities.Faculty.Attendance.ShowAttendanceActivity
import com.taskease.collegeapp.Activities.Faculty.Notes.ShowNotesActivity
import com.taskease.collegeapp.Activities.Faculty.Notice.AddNoticeActivity
import com.taskease.collegeapp.Activities.Faculty.SpreadSheet.ShowSpreadSheetActivity
import com.taskease.collegeapp.Activities.Faculty.Workload.ShowWorkLoadActivity
import com.taskease.collegeapp.Adapter.DashboardAdapter
import com.taskease.collegeapp.Constants.SharedPreferencesManager
import com.taskease.collegeapp.Model.DashboardItem
import com.taskease.collegeapp.R
import com.taskease.collegeapp.databinding.ActivityShowHostelMenusBinding

class ShowHostelMenusActivity : AppCompatActivity() {

    private lateinit var binding : ActivityShowHostelMenusBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowHostelMenusBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.actionBar.title.text = "Hostel"
        binding.actionBar.back.setOnClickListener{
            finish()
        }

        val hostelAllowance = SharedPreferencesManager.getBoolean(this@ShowHostelMenusActivity,SharedPreferencesManager.HOSTEL_ALLOWANCE)
        binding.recyclerView.layoutManager = LinearLayoutManager(this@ShowHostelMenusActivity)
        val list = ArrayList<DashboardItem>()
        if (hostelAllowance)
        {
            list.add(DashboardItem(R.drawable.penalty,"Penalty", ShowSpreadSheetActivity::class.java))
            list.add(DashboardItem(R.drawable.attendance,"Attendance", ShowAssignmentActivity::class.java))
            list.add(DashboardItem(R.drawable.admission,"Admission", AddNoticeActivity::class.java))
        }
        else
        {
            list.add(DashboardItem(R.drawable.admission,"Admission", AddAdmissionActivity::class.java))
        }

        binding.recyclerView.layoutManager = GridLayoutManager(this@ShowHostelMenusActivity,2, VERTICAL,false)
        binding.recyclerView.adapter = DashboardAdapter(this@ShowHostelMenusActivity,list)
    }
}