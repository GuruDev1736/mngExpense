package com.guru.demo

class UserRepository {

    suspend fun getUsers() = RetrofitInstance.api.getUsers()
}