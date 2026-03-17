package com.biobell.android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.biobell.android.ui.alarm.AlarmListScreen
import com.biobell.android.ui.alarm.AlarmSetterScreen
import com.biobell.android.ui.settings.SettingsScreen

import androidx.compose.ui.Modifier

/**
 * BioBell navigation graph.
 *
 * Destinations:
 * - [Screen.AlarmList]   — Home: list of alarms (bottom nav tab)
 * - [Screen.AlarmSetter] — Create/edit alarm (no bottom tab — FAB/card entry)
 * - [Screen.Settings]    — App settings (bottom nav tab)
 */
@Composable
fun BioBellNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Screen.AlarmList.route,
        modifier = modifier,
    ) {
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

        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}
