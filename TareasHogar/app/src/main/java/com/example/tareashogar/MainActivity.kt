package com.example.tareashogar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.tareashogar.analytics.AnalyticsTracker
import com.example.tareashogar.ui.AppRoot
import com.example.tareashogar.ui.theme.TareasHogarTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AnalyticsTracker.init(this)
        enableEdgeToEdge()
        setContent {
            TareasHogarTheme {
                AppRoot()
            }
        }
    }
}
