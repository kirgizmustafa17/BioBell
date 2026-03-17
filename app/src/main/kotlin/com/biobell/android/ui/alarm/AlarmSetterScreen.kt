package com.biobell.android.ui.alarm

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Alarm setter screen — placeholder for Phase 1.
 * Full implementation in Phase 5 (bidirectional sleep calculator).
 *
 * @param alarmId       Null for create mode, non-null for edit mode
 * @param onNavigateBack Called when user exits this screen
 */
@Composable
fun AlarmSetterScreen(
    alarmId: Long? = null,
    onNavigateBack: () -> Unit = {},
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (alarmId != null) "Edit Alarm" else "Set Alarm",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
