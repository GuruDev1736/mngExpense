package com.taskease.collegeapp.Activities

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import com.taskease.collegeapp.R
import com.taskease.collegeapp.databinding.ActivityOpenSpreadSheetBinding
import java.io.File
import java.net.URLConnection

class OpenSpreadSheetActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOpenSpreadSheetBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityOpenSpreadSheetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up action bar
        binding.actionBar.title.text = "Details"
        binding.actionBar.back.setOnClickListener {
            finish()
        }



        // Extract intent data
        val title = intent.getStringExtra("title") ?: "Spreadsheet"
        val description = intent.getStringExtra("description") ?: "No description"
        val link = intent.getStringExtra("link")
        val code = intent.getStringExtra("code")

        if (code.equals("bonafide"))
        {
            binding.openFile.visibility = View.GONE
        }

        // Set UI elements
        binding.titleName.text = title
        binding.description.text = description

        // Open file handling
        binding.openFile.setOnClickListener {
            link?.let { openLinkInChrome(this@OpenSpreadSheetActivity,it) }
        }
    }

    fun openLinkInChrome(context: Context, url: String) {
        val webpage: Uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, webpage)
        intent.setPackage("com.android.chrome")
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            val fallbackIntent = Intent(Intent.ACTION_VIEW, webpage)
            context.startActivity(fallbackIntent)
        }
    }
}