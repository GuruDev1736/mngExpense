package com.geniuskods.cosapp.Respository

import com.geniuskods.cosapp.Model.RequestModel.LoginRequestModel
import com.geniuskods.cosapp.Model.ResponseModel.LoginResponseModel
import com.geniuskods.cosapp.Network.Service.AuthApiService
import retrofit2.Response

class AuthRepository(private val api : AuthApiService) {
    suspend fun login(email: String, password: String) : Response<LoginResponseModel>{
        return api.login(LoginRequestModel(email,password))
    }
}