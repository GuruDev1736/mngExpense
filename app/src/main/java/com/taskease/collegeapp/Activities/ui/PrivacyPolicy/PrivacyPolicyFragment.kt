package com.taskease.collegeapp.Activities.ui.PrivacyPolicy

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.taskease.collegeapp.R
import com.taskease.collegeapp.databinding.FragmentPrivacyPolicyBinding

class PrivacyPolicyFragment : Fragment() {

    private lateinit var binding : FragmentPrivacyPolicyBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPrivacyPolicyBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.privacyPolicy.text = "**Privacy Policy**  \n" +
                "\n" +
                "Effective Date: [Insert Date]  \n" +
                "\n" +
                "**1. Introduction**  \n" +
                "Welcome to CampusFlow! This Privacy Policy describes how we collect, use, and protect the personal information of users of the CampusFlow mobile application developed for Government Polytechnic Gondia. By using our app, you agree to the terms of this Privacy Policy.\n" +
                "\n" +
                "**2. Information We Collect**  \n" +
                "We may collect the following types of information:\n" +
                "- **Personal Information:** Name, email address, student ID, or any details required for authentication.\n" +
                "- **Usage Data:** Logs of interactions within the app to improve functionality.\n" +
                "- **Device Information:** Device type, operating system, and app version.\n" +
                "\n" +
                "**3. How We Use Your Information**  \n" +
                "We use the collected data for:\n" +
                "- Providing and improving campus resource management.\n" +
                "- User authentication and access control.\n" +
                "- Enhancing app features and performance.\n" +
                "- Addressing user feedback and technical issues.\n" +
                "\n" +
                "**4. Data Sharing and Security**  \n" +
                "- We do not sell or share your personal information with third parties.\n" +
                "- Data is stored securely and protected from unauthorized access.\n" +
                "- Necessary security measures, such as encryption, are implemented to safeguard user data.\n" +
                "\n" +
                "**5. Third-Party Services**  \n" +
                "CampusFlow may integrate with third-party services for authentication or analytics. These services have their own privacy policies, and we encourage users to review them.\n" +
                "\n" +
                "**6. User Rights**  \n" +
                "You have the right to:\n" +
                "- Access, update, or delete your personal information.\n" +
                "- Request information on how your data is used.\n" +
                "- Opt out of certain data collection practices.\n" +
                "\n" +
                "**7. Changes to this Privacy Policy**  \n" +
                "We may update this policy from time to time. Any changes will be communicated through the app.\n" +
                "\n" +
                "**8. Contact Us**  \n" +
                "If you have any questions regarding this Privacy Policy, please contact us at gpgondia69@gmail.com.\n" +
                "\n" +
                "By using CampusFlow, you acknowledge that you have read and understood this Privacy Policy."
    }
}