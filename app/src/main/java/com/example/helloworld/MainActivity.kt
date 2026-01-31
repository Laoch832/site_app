package com.example.helloworld

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.example.helloworld.data.cache.ResourceCacheManager
import com.example.helloworld.ui.screens.MainScreen
import com.example.helloworld.ui.theme.HelloWorldTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var cacheManager: ResourceCacheManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            HelloWorldTheme {
                MainScreen(cacheManager = cacheManager)
            }
        }
    }
}
