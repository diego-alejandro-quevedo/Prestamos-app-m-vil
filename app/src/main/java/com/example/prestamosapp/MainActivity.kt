package com.example.prestamosapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.prestamosapp.ui.PrestamosApp
import com.example.prestamosapp.ui.theme.PrestamosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PrestamosTheme {
                PrestamosApp()
            }
        }
    }
}
