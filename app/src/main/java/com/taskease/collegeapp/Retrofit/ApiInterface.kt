package com.taskease.collegeapp.Retrofit

import com.taskease.collegeapp.Model.Request.LoginRequestModel
import com.taskease.collegeapp.Model.Response.BannerResponseModel
import com.taskease.collegeapp.Model.Response.LoginResponseModel
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiInterface {

    @POST("auth/login")
    fun login(@Body model : LoginRequestModel) : Call<LoginResponseModel>

    @GET("banner/")
    fun getBanners(@Header("Authorization") token : String) : Call<BannerResponseModel>
}