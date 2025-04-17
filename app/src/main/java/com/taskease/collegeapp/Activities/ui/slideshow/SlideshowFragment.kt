package com.taskease.collegeapp.Activities.ui.slideshow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.taskease.collegeapp.databinding.FragmentSlideshowBinding

class SlideshowFragment : Fragment() {

    private var _binding: FragmentSlideshowBinding? = null


    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSlideshowBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.aboutUs.text = "Welcome to CampusFlow – a smart campus management solution developed with pride by a team of passionate students from Government Polytechnic, Gondia.\n" +
                "\n" +
                "We are a group of six dedicated students — Om K. Patil, Satish Patil, Khushi Jaiswal, Sonam Mate, Sumeet Patil, and Ashil Deshmukh — who came together with a shared vision to streamline and enhance the campus experience for students, faculty, and administration alike.\n" +
                "\n" +
                "CampusFlow is designed to simplify various campus-related activities and improve communication, efficiency, and organization within our college. From notices and timetables to event updates and more, CampusFlow is your go-to digital companion on campus.\n" +
                "\n" +
                "This project was developed under the valuable guidance of our mentor, Mrs. Bhagyashree Meshram (Fulzhele), whose support and insights have been instrumental throughout our journey.\n" +
                "\n" +
                "We hope CampusFlow serves as a helpful tool for our institution and inspires future innovations within our college community.\n" +
                "\n" +
                "— Team CampusFlow"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}