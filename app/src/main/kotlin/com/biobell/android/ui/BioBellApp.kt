package com.biobell.android.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.biobell.android.ui.navigation.BioBellBottomBar
import com.biobell.android.ui.navigation.BioBellNavGraph
import com.biobell.android.ui.navigation.Screen

/**
 * Root composable — hosts the navigation graph and bottom bar.
 *
 * On first launch, routes to [Screen.PermissionSetup] instead of [Screen.AlarmList].
 * The bottom bar is hidden during the setup flow.
 */
@Composable
fun BioBellApp(viewModel: BioBellAppViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentRoute by navController.currentBackStackEntryAsState()

    // Wait until we've read the DataStore (avoids brief "main screen flash" on first launch)
    if (uiState.isLoading) return

    val startDestination = if (uiState.isOnboardingComplete)
        Screen.AlarmList.route
    else
        Screen.PermissionSetup.route

    // Hide bottom bar on setup screen
    val showBottomBar = currentRoute?.destination?.route != Screen.PermissionSetup.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BioBellBottomBar(navController = navController)
            }
        },
    ) { innerPadding ->
        BioBellNavGraph(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
        )
    }
}
