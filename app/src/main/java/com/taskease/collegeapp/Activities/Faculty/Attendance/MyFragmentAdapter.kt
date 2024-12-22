package com.taskease.collegeapp.Activities.Faculty.Attendance

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter


class MyFragmentAdapter(fa: FragmentActivity?) : FragmentStateAdapter(fa!!) {
    override fun createFragment(position: Int): Fragment {
        // Return the corresponding fragment based on the tab selected
        return if (position == 0) {
            TeacherFragment() // Teacher Fragment
        } else {
            StudentFragment() // Student Fragment
        }
    }

    override fun getItemCount(): Int {
        return 2 // We have two tabs (Teacher and Student)
    }
}
