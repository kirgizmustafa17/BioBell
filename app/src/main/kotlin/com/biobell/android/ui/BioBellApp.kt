package com.biobell.android.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.biobell.android.ui.navigation.BioBellBottomBar
import com.biobell.android.ui.navigation.BioBellNavGraph

/**
 * Root composable that hosts the navigation graph and bottom bar.
 * Rendered inside [BioBellTheme] from [MainActivity].
 */
@Composable
fun BioBellApp() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BioBellBottomBar(navController = navController)
        },
    ) { innerPadding ->
        BioBellNavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
        )
    }
}
