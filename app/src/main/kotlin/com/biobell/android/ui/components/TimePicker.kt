package com.biobell.android.ui.components

import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

/**
 * A tappable time display that opens the system TimePickerDialog on click.
 *
 * @param label      Label shown above the time (e.g. "Wake time" or "Bedtime")
 * @param time       Currently selected time
 * @param onTimeSet  Called with the updated [LocalDateTime] when user confirms the picker
 * @param isHighlighted  Whether this field is the active/focused input mode
 */
@Composable
fun TimePicker(
    label: String,
    time: LocalDateTime,
    onTimeSet: (LocalDateTime) -> Unit,
    isHighlighted: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val containerColor = if (isHighlighted)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surfaceVariant

    val contentColor = if (isHighlighted)
        MaterialTheme.colorScheme.onPrimaryContainer
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = modifier
            .clickable {
                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        onTimeSet(time.withHour(hour).withMinute(minute).withSecond(0).withNano(0))
                    },
                    time.hour,
                    time.minute,
                    true, // is24Hour
                ).show()
            },
        shape = MaterialTheme.shapes.large,
        color = containerColor,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor,
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = time.format(timeFormatter),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isHighlighted)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
