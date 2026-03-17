package com.biobell.android.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room database entity representing a stored alarm.
 *
 * Uses primitive types and strings for Room compatibility.
 * Mapped to/from [com.biobell.android.domain.model.Alarm] via [AlarmMapper].
 *
 * [repeatDays] — comma-separated DayOfWeek names (e.g. "MONDAY,WEDNESDAY,FRIDAY")
 *   Empty string = one-shot alarm.
 * [wakeTimeEpochMilli] — milliseconds since epoch for the next scheduled wake.
 *   Stored as epoch so Room can sort and query without time parsing.
 */
@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val wakeTimeEpochMilli: Long,       // next scheduled wake time
    val wakeTimeHour: Int,              // stored separately for display (avoids TZ conversion)
    val wakeTimeMinute: Int,
    val label: String = "",
    val isEnabled: Boolean = true,
    val repeatDays: String = "",        // "MONDAY,WEDNESDAY" or "" for one-shot
    val ringtoneUri: String? = null,
    val isVibrate: Boolean = true,
    val snoozeDurationMinutes: Int = 9,
    // Sleep plan summary fields (denormalized for simplicity — no separate table needed)
    val sleepPlanBedtimeEpoch: Long? = null,
    val sleepPlanDurationMinutes: Int? = null,
    val sleepPlanCycles: Int? = null,
    val sleepPlanHealthScore: Int? = null,
    val sleepPlanGrade: String? = null,  // "A", "B", "C", "D", "F"
    val chronotype: String = "INTERMEDIATE",  // Chronotype.name
)
