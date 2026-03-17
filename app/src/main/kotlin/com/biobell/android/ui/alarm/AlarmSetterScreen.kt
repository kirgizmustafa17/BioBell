package com.biobell.android.ui.alarm

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.biobell.android.ui.components.HealthBadge
import com.biobell.android.ui.components.SleepCycleCard
import com.biobell.android.ui.components.TimePicker
import com.biobell.android.ui.components.WarningSummary
import java.time.LocalDateTime

/**
 * Alarm setter screen — BioBell's core UX.
 *
 * Features:
 * - Bidirectional time pickers (wake time ↔ bedtime)
 * - Auto-computed sleep cycle suggestions (sorted best→worst)
 * - Tappable suggestion cards with health grade
 * - Health badge showing selected plan score
 * - Inline sleep warnings (animated in/out)
 * - Label + vibration options
 * - Save / Cancel actions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmSetterScreen(
    alarmId: Long? = null,
    onNavigateBack: () -> Unit = {},
    viewModel: AlarmSetterViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.isEditing) "Edit Alarm" else "New Alarm",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.onCancel(onNavigateBack) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.onSave(onNavigateBack) },
                        enabled = uiState.canSave,
                    ) {
                        Text(
                            text = "Save",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ── Time pickers (Wake & Bedtime) ─────────────────────────────
            item(key = "time_pickers") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    TimePicker(
                        label = "Wake time",
                        time = uiState.wakeTime,
                        onTimeSet = { newTime ->
                            viewModel.onWakeTimeChanged(
                                LocalDateTime.of(uiState.wakeTime.toLocalDate(), newTime.toLocalTime())
                            )
                        },
                        isHighlighted = uiState.inputMode == InputMode.WAKE_TIME,
                        modifier = Modifier.weight(1f),
                    )
                    TimePicker(
                        label = "Bedtime",
                        time = uiState.bedtime ?: uiState.wakeTime,
                        onTimeSet = { newTime ->
                            viewModel.onBedtimeChanged(
                                LocalDateTime.of(uiState.wakeTime.toLocalDate(), newTime.toLocalTime())
                            )
                        },
                        isHighlighted = uiState.inputMode == InputMode.BEDTIME,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // ── Health badge (selected plan) ──────────────────────────────
            item(key = "health_badge") {
                uiState.selectedPlan?.let { plan ->
                    HealthBadge(
                        score = plan.healthScore.score,
                        grade = plan.healthScore.grade,
                        summary = plan.healthScore.summary,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            // ── Chronotype hint ───────────────────────────────────────────
            item(key = "chronotype_hint") {
                val chronotype = uiState.chronotype
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "${chronotype.emoji} ${chronotype.label} — suggestions shifted ${
                            if (chronotype.offsetMinutes == 0) "by 0 min"
                            else "${if (chronotype.offsetMinutes > 0) "+" else ""}${chronotype.offsetMinutes} min"
                        }",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                }
            }

            // ── Section header ────────────────────────────────────────────
            item(key = "suggestions_header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Bedtime,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "Sleep cycle suggestions",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            // ── Suggestion cards ──────────────────────────────────────────
            items(
                items = uiState.suggestions,
                key = { plan -> "${plan.cycles}_${plan.bedtime}_${plan.wakeTime}" },
            ) { plan ->
                SleepCycleCard(
                    plan = plan,
                    isSelected = uiState.selectedPlan?.let {
                        it.cycles == plan.cycles && it.bedtime == plan.bedtime
                    } ?: false,
                    onClick = { viewModel.onPlanSelected(plan) },
                )
            }

            // ── Warnings ──────────────────────────────────────────────────
            if (uiState.warnings.isNotEmpty()) {
                item(key = "warnings") {
                    WarningSummary(
                        warnings = uiState.warnings,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            // ── Divider + Options ─────────────────────────────────────────
            item(key = "divider") {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            }

            // Label field
            item(key = "label_field") {
                OutlinedTextField(
                    value = uiState.alarmLabel,
                    onValueChange = viewModel::onLabelChanged,
                    label = { Text("Label (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                )
            }

            // Vibration toggle
            item(key = "vibration_toggle") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Vibrate",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Switch(
                        checked = uiState.isVibrate,
                        onCheckedChange = viewModel::onVibrateToggled,
                    )
                }
            }

            // Bottom spacing
            item(key = "bottom_space") {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
