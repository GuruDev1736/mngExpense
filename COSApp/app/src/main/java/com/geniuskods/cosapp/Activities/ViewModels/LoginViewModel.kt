package com.geniuskods.cosapp.Activities.ViewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geniuskods.cosapp.Model.ResponseModel.LoginResponseModel
import com.geniuskods.cosapp.Respository.AuthRepository
import com.geniuskods.cosapp.Utils.Spref
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: AuthRepository) : ViewModel() {

//    var isLoading by mutableStateOf(false)

    private val _loginData = MutableStateFlow<LoginResponseModel?>(null)
    val loginData = _loginData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()


//    fun login(email: String, password: String, onResult: (LoginResponseModel?) -> Unit) {
//        viewModelScope.launch {
//            try {
//                isLoading = true
//                val response = repository.login(email, password)
//                if (response.isSuccessful && response.body() != null) {
//                    val responseData = response.body()
//                    if (responseData?.status == "200") {
//                        storeSharedPrefData(
//                            email = email,
//                            password = password,
//                            token = responseData.data.token,
//                            userId = responseData.data.userId
//                        )
//                        onResult(responseData)
//                    } else {
//                        onResult(responseData)
//                    }
//                }
//            } catch (e: Exception) {
//                Log.d("ERROR", "Login failed: ${e.message}")
//                onResult(null)
//            } finally {
//                isLoading = false
//            }
//        }
//    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val response = repository.login(email, password)
                if (response.isSuccessful && response.body()?.status == "200") {
                    val data = response.body()!!
                    storeSharedPrefData(email, password, data.data.token, data.data.userId)
                    _loginData.value = data
                } else {
                    _loginData.value = response.body()
                }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun storeSharedPrefData(email: String, password: String, token: String, userId: Int) {
        Spref.putString(Spref.EMAIL, email)
        Spref.putString(Spref.PASSWORD, password)
        Spref.putString("token", token)
        Spref.putInt("userId", userId)
    }


}