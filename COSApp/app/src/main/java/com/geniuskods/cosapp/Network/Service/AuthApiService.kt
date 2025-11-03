package com.geniuskods.cosapp.Network.Service

import com.geniuskods.cosapp.Model.RequestModel.LoginRequestModel
import com.geniuskods.cosapp.Model.ResponseModel.LoginResponseModel
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("entry-service/api/auth/login")
    suspend fun login(@Body model : LoginRequestModel) : Response<LoginResponseModel>

}