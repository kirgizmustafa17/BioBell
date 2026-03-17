package com.biobell.android.ui.alarm

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Alarm list screen — placeholder for Phase 1.
 * Full implementation in Phase 6.
 *
 * @param onCreateAlarm Called when user taps FAB to create a new alarm
 * @param onEditAlarm   Called when user taps an existing alarm card (with ID)
 */
@Composable
fun AlarmListScreen(
    onCreateAlarm: () -> Unit = {},
    onEditAlarm: (Long) -> Unit = {},
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Alarms",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
