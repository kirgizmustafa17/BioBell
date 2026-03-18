package com.biobell.android.ui.navigation

/**
 * Sealed class representing all navigation destinations in BioBell.
 *
 * Routes:
 * - AlarmList   : Home screen — list of scheduled alarms
 * - AlarmSetter : Create/edit alarm — reached via FAB or alarm card tap
 * - Settings    : App settings — chronotype, preferences
 */
sealed class Screen(val route: String) {

    /** Home screen — list of all alarms */
    object AlarmList : Screen("alarm_list")

    /** Create or edit an alarm. Optional alarmId for edit mode. */
    object AlarmSetter : Screen("alarm_setter?alarmId={alarmId}") {
        const val ARG_ALARM_ID = "alarmId"

        /** Navigate to create mode (no ID) or edit mode (with ID) */
        fun createRoute(alarmId: Long? = null): String =
            if (alarmId != null) "alarm_setter?alarmId=$alarmId" else "alarm_setter"
    }

    /** Settings — chronotype, preferences, about */
    object Settings : Screen("settings")

    /** First-launch permission onboarding */
    object PermissionSetup : Screen("permission_setup")
}

/** Model for bottom navigation bar items */
data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val contentDescription: String,
)
