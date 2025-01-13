package com.taskease.collegeapp.Activities.SuperAdmin.TimeTables

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter


class TimeTableViewPagerAdapter(fa: FragmentActivity?) : FragmentStateAdapter(fa!!) {
    override fun createFragment(position: Int): Fragment {
        // Return the corresponding fragment based on the tab selected
        return if (position == 0) {
            ExamFragment() // Teacher Fragment
        } else {
            RegularFragment() // Student Fragment
        }
    }

    override fun getItemCount(): Int {
        return 2 // We have two tabs (Teacher and Student)
    }
}
