package com.biobell.android.domain.model

import java.time.LocalTime

/**
 * User's chronotype — determines how sleep suggestions are shifted.
 *
 * @property offsetMinutes How many minutes to shift suggestions from baseline.
 *   Positive = shift later (night owl), negative = shift earlier (early bird).
 */
enum class Chronotype(val offsetMinutes: Int, val emoji: String, val label: String) {
    EARLY_BIRD(offsetMinutes = -30, emoji = "🐓", label = "Early Bird"),
    INTERMEDIATE(offsetMinutes = 0, emoji = "🐦", label = "Intermediate"),
    NIGHT_OWL(offsetMinutes = +30, emoji = "🦉", label = "Night Owl"),
}
