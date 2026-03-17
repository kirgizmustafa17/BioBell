package com.biobell.android.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biobell.android.domain.model.Chronotype

/**
 * Tappable card for selecting a chronotype.
 *
 * Displays the emoji, label, and a brief description.
 * Animates selection with color + border.
 */
@Composable
fun ChronotypeCard(
    chronotype: Chronotype,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(200),
        label = "chronotype_bg",
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.primary
        else
            Color.Transparent,
        animationSpec = tween(200),
        label = "chronotype_border",
    )

    Surface(
        modifier = modifier
            .border(
                width = if (isSelected) 1.5.dp else 0.dp,
                color = borderColor,
                shape = MaterialTheme.shapes.large,
            )
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        color = containerColor,
        tonalElevation = if (isSelected) 0.dp else 1.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = chronotype.emoji,
                fontSize = 32.sp,
            )
            Text(
                text = chronotype.label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = chronotypeDescription(chronotype),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

private fun chronotypeDescription(chronotype: Chronotype): String = when (chronotype) {
    Chronotype.EARLY_BIRD  -> "Naturally early\n−30 min shift"
    Chronotype.INTERMEDIATE -> "Neither extreme\nNo shift"
    Chronotype.NIGHT_OWL   -> "Naturally late\n+30 min shift"
}
