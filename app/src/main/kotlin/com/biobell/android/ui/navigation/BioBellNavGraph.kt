package com.biobell.android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.biobell.android.ui.alarm.AlarmListScreen
import com.biobell.android.ui.alarm.AlarmSetterScreen
import com.biobell.android.ui.settings.SettingsScreen
import com.biobell.android.ui.setup.PermissionSetupScreen

/**
 * BioBell navigation graph.
 *
 * Destinations:
 * - [Screen.PermissionSetup] — First-launch permission onboarding (conditional start)
 * - [Screen.AlarmList]       — Home: list of alarms (bottom nav tab)
 * - [Screen.AlarmSetter]     — Create/edit alarm (no bottom tab — FAB/card entry)
 * - [Screen.Settings]        — App settings (bottom nav tab)
 */
@Composable
fun BioBellNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.AlarmList.route,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        // ── Permission onboarding (first launch only) ─────────────────────
        composable(Screen.PermissionSetup.route) {
            PermissionSetupScreen(
                onSetupComplete = {
                    navController.navigate(Screen.AlarmList.route) {
                        // Remove setup from back stack so Back doesn't return to it
                        popUpTo(Screen.PermissionSetup.route) { inclusive = true }
                    }
                },
            )
        }

        // ── Alarm list (home) ─────────────────────────────────────────────
        composable(Screen.AlarmList.route) {
            AlarmListScreen(
                onCreateAlarm = {
                    navController.navigate(Screen.AlarmSetter.createRoute())
                },
                onEditAlarm = { alarmId ->
                    navController.navigate(Screen.AlarmSetter.createRoute(alarmId))
                },
            )
        }

        // ── Alarm setter (create / edit) ──────────────────────────────────
        composable(
            route = Screen.AlarmSetter.route,
            arguments = listOf(
                navArgument(Screen.AlarmSetter.ARG_ALARM_ID) {
                    type = NavType.LongType
                    defaultValue = -1L
                },
            ),
        ) { backStackEntry ->
            val rawId = backStackEntry.arguments?.getLong(Screen.AlarmSetter.ARG_ALARM_ID) ?: -1L
            val alarmId = if (rawId == -1L) null else rawId

            AlarmSetterScreen(
                alarmId = alarmId,
                onNavigateBack = { navController.popBackStack() },
            )
        }

        // ── Settings ──────────────────────────────────────────────────────
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}
