package com.geniuskods.cosapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import com.geniuskods.cosapp.Activities.Auth.LoginActivity
import com.geniuskods.cosapp.Utils.Spref
import com.geniuskods.cosapp.ui.theme.COSAppTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            Spref.init(context)
            COSAppTheme {
                App()
            }
        }
    }
}

@Composable
fun App() {
    AppSplashScreen()
}

@Composable
fun AppSplashScreen() {
    var alpha by remember { mutableFloatStateOf(0f) }
    val animatedAlpha = animateFloatAsState(targetValue = alpha, label = "")
    val context = LocalContext.current

    // Launch a timer for 2 seconds
    LaunchedEffect(Unit) {
        alpha = 1f
        delay(2000)
        context.startActivity(Intent(context, LoginActivity::class.java))
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // App logo (replace with your own image)
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(120.dp)
                    .alpha(animatedAlpha.value)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "COSApp",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.alpha(animatedAlpha.value)
            )
        }
    }
}
