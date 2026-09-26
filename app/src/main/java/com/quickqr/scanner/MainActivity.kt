package com.quickqr.scanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.quickqr.scanner.ui.navigation.QuickQrNavHost
import com.quickqr.scanner.ui.theme.QuickQrTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuickQrTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    QuickQrNavHost()
                }
            }
        }
    }
}
