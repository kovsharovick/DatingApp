package com.example.androiddatingapp

import android.os.Bundle
import android.util.DisplayMetrics
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.androiddatingapp.ui.AppRoot
import com.example.androiddatingapp.ui.model.ScreenInfo
import com.example.androiddatingapp.ui.theme.AndroidDatingAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val displayMetrics = DisplayMetrics().also { dm ->
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getRealMetrics(dm)
        }
        val screen = ScreenInfo(
            widthPx = displayMetrics.widthPixels,
            heightPx = displayMetrics.heightPixels,
            density = displayMetrics.density
        )

        setContent {
            AndroidDatingAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Surface(Modifier.fillMaxSize().padding(innerPadding)) {
                        AppRoot(screen = screen)
                    }
                }
            }
        }
    }
}

