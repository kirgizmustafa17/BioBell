package com.biobell.android.domain.model

import java.time.LocalDateTime
import java.time.LocalTime

/**
 * A resolved sleep plan: the user's intended bedtime, wake time,
 * resulting sleep duration, and how many full 90-min cycles that represents.
 *
 * All times are wall-clock LocalDateTime (not LocalTime) to handle midnight crossovers correctly.
 */
data class SleepPlan(
    val bedtime: LocalDateTime,
    val wakeTime: LocalDateTime,
    val durationMinutes: Int,       // Net usable sleep (after onset offset)
    val cycles: Int,                // How many full 90-min cycles fit
    val chronotype: Chronotype,
    val healthScore: HealthScore,
    val warnings: List<SleepWarning>,
) {
    val durationHours: Double get() = durationMinutes / 60.0
    val isHealthy: Boolean get() = warnings.none { it.severity == WarningSeverity.ERROR }
}
