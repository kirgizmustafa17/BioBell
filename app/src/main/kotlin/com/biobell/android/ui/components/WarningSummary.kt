package com.biobell.android.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.biobell.android.domain.model.SleepWarning
import com.biobell.android.domain.model.WarningSeverity

/**
 * Animated list of sleep health warnings shown below the suggestion cards.
 * Appears/disappears smoothly when warnings change.
 */
@Composable
fun WarningSummary(
    warnings: List<SleepWarning>,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = warnings.isNotEmpty(),
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            warnings.forEach { warning ->
                WarningRow(warning = warning)
            }
        }
    }
}

@Composable
private fun WarningRow(warning: SleepWarning) {
    val (icon, color) = when (warning.severity) {
        WarningSeverity.ERROR   -> Pair(Icons.Outlined.ErrorOutline, MaterialTheme.colorScheme.error)
        WarningSeverity.WARNING -> Pair(Icons.Outlined.Warning, Color(0xFFF9A825))
        WarningSeverity.INFO    -> Pair(Icons.Outlined.Info, MaterialTheme.colorScheme.onSurfaceVariant)
    }

    Surface(
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.08f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = warning.severity.name,
                tint = color,
                modifier = Modifier.size(16.dp).padding(top = 1.dp),
            )
            Text(
                text = warning.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
