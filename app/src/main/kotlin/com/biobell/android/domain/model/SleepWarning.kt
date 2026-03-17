package com.biobell.android.domain.model

/**
 * Severity level of a sleep health warning.
 */
enum class WarningSeverity { ERROR, WARNING, INFO }

/**
 * A specific health concern with the user's configured sleep plan.
 * Shown as inline tips on the AlarmSetter screen.
 */
data class SleepWarning(
    val code: WarningCode,
    val severity: WarningSeverity,
    val message: String,
) {
    enum class WarningCode {
        DURATION_BELOW_MINIMUM,      // < 6 hours
        DURATION_BELOW_RECOMMENDED,  // 6–7h30m
        MID_CYCLE_WAKE,              // Wake time not on a 90-min cycle boundary
        CHRONOTYPE_MISMATCH,         // Wake time conflicts with chronotype
        SLEEP_ONSET_TOO_LATE,        // Bedtime past 2am for early bird / intermediate
    }
}
