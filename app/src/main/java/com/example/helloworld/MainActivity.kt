package com.example.helloworld

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.example.helloworld.ui.screens.MainScreen
import com.example.helloworld.ui.theme.HelloWorldTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            HelloWorldTheme {
                MainScreen()
            }
        }
    }
}
