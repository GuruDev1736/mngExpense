package com.guru.task_1.API

import com.guru.task_1.Model.UserResponseModel
import retrofit2.Call
import retrofit2.http.GET

interface ApiService {

    @GET("users")
    fun getUsers() : Call<UserResponseModel>

}