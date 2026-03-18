package com.biobell.android.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.biobell.android.domain.model.HealthScore
import com.biobell.android.domain.model.SleepPlan
import java.time.format.DateTimeFormatter

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

/**
 * A tappable card representing a single sleep plan suggestion.
 *
 * Shows:
 * - Bedtime and wake time
 * - Number of cycles and duration
 * - Health grade chip
 *
 * Animates background when selected.
 */
@Composable
fun SleepCycleCard(
    plan: SleepPlan,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(durationMillis = 200),
        label = "card_color",
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.primary
        else
            Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "border_color",
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .border(
                width = if (isSelected) 1.5.dp else 0.dp,
                color = borderColor,
                shape = MaterialTheme.shapes.large,
            )
            .clickable(onClick = onClick),
        color = containerColor,
        shape = MaterialTheme.shapes.large,
        tonalElevation = if (isSelected) 0.dp else 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left: times and cycle info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = plan.bedtime.format(timeFormatter),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "  →  ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = plan.wakeTime.format(timeFormatter),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurface,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${plan.cycles} cycles · ${formatDuration(plan.durationMinutes)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Right: health grade chip
            HealthGradeChip(grade = plan.healthScore.grade, isSelected = isSelected)
        }
    }
}

/**
 * Compact chip showing the letter grade (A/B/C/D/F) with grade-appropriate color.
 */
@Composable
fun HealthGradeChip(
    grade: HealthScore.Grade,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
) {
    val (bg, fg) = gradeColors(grade)

    Surface(
        modifier = modifier.size(width = 40.dp, height = 40.dp),
        shape = MaterialTheme.shapes.medium,
        color = bg.copy(alpha = if (isSelected) 1f else 0.85f),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = grade.label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = fg,
            )
        }
    }
}

/**
 * Larger health badge shown at the top of the screen summarizing the selected plan.
 */
@Composable
fun HealthBadge(
    score: Int,
    grade: HealthScore.Grade,
    summary: String,
    modifier: Modifier = Modifier,
) {
    val (bg, fg) = gradeColors(grade)

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        color = bg.copy(alpha = 0.15f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Score circle
            Surface(
                modifier = Modifier.size(44.dp),
                shape = MaterialTheme.shapes.medium,
                color = bg,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = grade.label,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = fg,
                    )
                }
            }
            Column {
                Text(
                    text = "Sleep Health",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
internal fun gradeColors(grade: HealthScore.Grade): Pair<Color, Color> {
    val colors = MaterialTheme.colorScheme
    return when (grade) {
        HealthScore.Grade.A -> Pair(Color(0xFF2E7D32), Color.White)  // green
        HealthScore.Grade.B -> Pair(Color(0xFF388E3C), Color.White)  // light green
        HealthScore.Grade.C -> Pair(Color(0xFFF9A825), Color(0xFF1A1A1A))  // amber
        HealthScore.Grade.D -> Pair(Color(0xFFE65100), Color.White)  // deep orange
        HealthScore.Grade.F -> Pair(Color(0xFFC62828), Color.White)  // red
    }
}

private fun formatDuration(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return if (m == 0) "${h}h" else "${h}h ${m}m"
}
