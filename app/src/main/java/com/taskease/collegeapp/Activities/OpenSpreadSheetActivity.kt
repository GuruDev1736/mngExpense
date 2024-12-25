package com.taskease.collegeapp.Activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.taskease.collegeapp.R
import com.taskease.collegeapp.databinding.ActivityOpenSpreadSheetBinding
import com.taskease.collegeapp.databinding.ActivityShowBudgetBinding

class OpenSpreadSheetActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOpenSpreadSheetBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOpenSpreadSheetBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.actionBar.title.text = "Details"
        binding.actionBar.back.setOnClickListener {
            finish()
        }

        val title = intent.getStringExtra("title")
        val description = intent.getStringExtra("description")
        val link = intent.getStringExtra("link")

        binding.titleName.text = title
        binding.description.text = description
        binding.openFile.setOnClickListener{
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent)
                } else {
                    Toast.makeText(this@OpenSpreadSheetActivity, "No app found to open the link", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this@OpenSpreadSheetActivity, "Error opening the link", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }
}