package com.biobell.android.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.biobell.android.domain.model.Alarm
import com.biobell.android.ui.components.gradeColors
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

/**
 * Card representing a single alarm in the list.
 *
 * Shows:
 * - Wake time (large) with label
 * - Enabled/disabled toggle
 * - Bedtime if a sleep plan is attached
 * - Repeat days (Mon, Wed, Fri…)
 * - Health grade chip if sleep plan present
 *
 * Dimmed when disabled. Tappable to edit.
 */
@Composable
fun AlarmCard(
    alarm: Alarm,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentAlpha = if (alarm.isEnabled) 1f else 0.5f
    val surfaceColor by animateColorAsState(
        targetValue = if (alarm.isEnabled)
            MaterialTheme.colorScheme.surfaceVariant
        else
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        animationSpec = tween(200),
        label = "alarm_card_color",
    )

    Surface(
        onClick = onEdit,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = surfaceColor,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left section: times + info
            Column(modifier = Modifier.weight(1f)) {

                // Wake time
                Text(
                    text = alarm.wakeTime.format(timeFormatter),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
                )

                // Label (if set)
                if (alarm.label.isNotBlank()) {
                    Text(
                        text = alarm.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha),
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Sleep plan information row
                alarm.sleepPlan?.let { plan ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Bedtime,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = contentAlpha),
                        )
                        Text(
                            text = "Bed ${plan.bedtime.format(timeFormatter)} · ${plan.cycles} cycles",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha),
                        )
                        // Health grade chip
                        Surface(
                            shape = MaterialTheme.shapes.extraSmall,
                            color = gradeColors(plan.healthScore.grade).first
                                .copy(alpha = if (alarm.isEnabled) 0.85f else 0.4f),
                        ) {
                            Text(
                                text = plan.healthScore.grade.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = gradeColors(plan.healthScore.grade).second,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                            )
                        }
                    }
                }

                // Repeat days
                if (alarm.isRepeating) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formatRepeatDays(alarm.repeatDays),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha),
                    )
                } else {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "One-time",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha),
                    )
                }
            }

            // Right: toggle switch
            Switch(
                checked = alarm.isEnabled,
                onCheckedChange = onToggle,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}


private fun formatRepeatDays(days: Set<DayOfWeek>): String {
    if (days.size == 7) return "Every day"
    if (days == setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) return "Weekdays"
    if (days == setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)) return "Weekends"
    return days.sortedBy { it.value }
        .joinToString(", ") { it.getDisplayName(TextStyle.SHORT, Locale.getDefault()) }
}
