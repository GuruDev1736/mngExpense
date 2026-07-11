package com.guru.task_1

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.guru.task_1.API.RetrofitInstance
import com.guru.task_1.Adapters.UserAdapter
import com.guru.task_1.Model.UserResponseModel
import com.guru.task_1.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)


        val name =
            "leetcode".groupingBy { it -> Log.d("char", it.toString()) }.eachCount()
                .filter { it.value == 1 }




        Log.d("OUTPUT", "OutPut :- $name")






        binding.recView.layoutManager = LinearLayoutManager(this)


        try {
            RetrofitInstance.getInstance().getUsers().enqueue(object : Callback<UserResponseModel> {
                override fun onResponse(
                    call: Call<UserResponseModel?>, response: Response<UserResponseModel?>
                ) {
                    if (response.isSuccessful) {
                        val data = response.body()
                        if (data != null) {
                            binding.recView.adapter = UserAdapter(data)
                        }
                    } else {
                        Toast.makeText(
                            this@MainActivity, "Something went wrong", Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(
                    p0: Call<UserResponseModel?>, p1: Throwable
                ) {
                    Toast.makeText(this@MainActivity, "Something went wrong", Toast.LENGTH_SHORT)
                        .show()
                    Log.d("Failure", p1.message.toString())
                }

            })
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("Exception", e.message.toString())
        }


    }

}