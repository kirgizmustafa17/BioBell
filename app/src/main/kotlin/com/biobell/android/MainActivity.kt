package com.biobell.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.biobell.android.ui.BioBellApp
import com.biobell.android.ui.theme.BioBellTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single activity host for the BioBell app.
 * Hilt entry point — injects dependencies into the activity graph.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BioBellTheme {
                BioBellApp()
            }
        }
    }
}
