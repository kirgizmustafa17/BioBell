package com.biobell.android.domain.model

import java.time.DayOfWeek
import java.time.LocalDateTime

/**
 * Core alarm domain model.
 *
 * An alarm fires at [wakeTime] and was scheduled based on a [sleepPlan].
 * When [sleepPlan] is null the alarm is a simple one-shot time alarm (no biology).
 *
 * [repeatDays] is a set of [DayOfWeek]; empty = one-shot alarm.
 * [isEnabled] controls whether AlarmManager has this scheduled.
 */
data class Alarm(
    val id: Long = 0L,
    val wakeTime: LocalDateTime,
    val label: String = "",
    val isEnabled: Boolean = true,
    val repeatDays: Set<DayOfWeek> = emptySet(),
    val ringtoneUri: String? = null,   // null = system default alarm tone
    val isVibrate: Boolean = true,
    val snoozeDurationMinutes: Int = 9,
    val sleepPlan: SleepPlan? = null,  // null if alarm was set without biology
) {
    val isRepeating: Boolean get() = repeatDays.isNotEmpty()
}
