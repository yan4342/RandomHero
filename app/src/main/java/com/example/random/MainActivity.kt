package com.example.random

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.random.data.AppSettingsRepository
import com.example.random.ui.RandomHeroApp
import com.example.random.ui.theme.RandomTheme



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // 恢复应用的主题，替换掉开屏主题
        setTheme(R.style.Theme_Random)
        enableEdgeToEdge()
        
        super.onCreate(savedInstanceState)
        setContent {
            val themeColors by AppSettingsRepository.themeColors.collectAsState()

            RandomTheme(themeColors = themeColors) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RandomHeroApp()
                }
            }
        }
    }
}
