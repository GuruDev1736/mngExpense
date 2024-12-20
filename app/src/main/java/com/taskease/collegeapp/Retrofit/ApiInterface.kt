package com.taskease.collegeapp.Retrofit

import com.taskease.collegeapp.Model.Request.LoginRequestModel
import com.taskease.collegeapp.Model.Response.BannerResponseModel
import com.taskease.collegeapp.Model.Response.LoginResponseModel
import com.taskease.collegeapp.Model.Response.SpreadSheetResponseModel
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiInterface {

    @POST("auth/login")
    fun login(@Body model : LoginRequestModel) : Call<LoginResponseModel>

    @GET("banner/")
    fun getBanners(@Header("Authorization") token : String) : Call<BannerResponseModel>

    @GET("spreadsheet/")
    fun getAllSpreadSheet(@Header("Authorization") token : String , @Query("category") category : String , @Query("department") departmentId : Int) : Call<SpreadSheetResponseModel>
}