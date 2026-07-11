package com.geniuskods.cosapp.Activities.Auth

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.geniuskods.cosapp.ViewModels.LoginViewModel
import com.geniuskods.cosapp.Components.AppTopBar
import com.geniuskods.cosapp.Components.CustomButton
import com.geniuskods.cosapp.Components.CustomTextField
import com.geniuskods.cosapp.Network.ApiClient
import com.geniuskods.cosapp.Respository.AuthRepository
import com.geniuskods.cosapp.Utils.Spref
import com.geniuskods.cosapp.ui.theme.COSAppTheme

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val repository = AuthRepository(ApiClient.getInstance())
            val viewModel = LoginViewModel(repository)
            COSAppTheme {
                LoginScreen(viewModel)
            }
        }
    }
}

@Composable
fun LoginScreen(viewModel: LoginViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    val loginData by viewModel.loginData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    email = Spref.getString(Spref.EMAIL)
    password = Spref.getString(Spref.PASSWORD)

    Scaffold(
        topBar = { AppTopBar(title = "Login") }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CustomTextField(value = email, onValueChange = { email = it }, label = "Email")
            Spacer(modifier = Modifier.height(16.dp))
            CustomTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                isPassword = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            CustomButton(
                text = if (isLoading) "Logging in..." else "Login",
                isLoading = isLoading,
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.login(email, password)
                    }
                })

            LaunchedEffect(loginData) {
                loginData?.let {
                    Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

