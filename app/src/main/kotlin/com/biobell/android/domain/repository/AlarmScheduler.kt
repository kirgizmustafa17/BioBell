package com.biobell.android.domain.repository

import com.biobell.android.domain.model.Alarm

/**
 * Contract for scheduling alarms with the Android AlarmManager.
 *
 * The implementation lives in alarm/AlarmSchedulerImpl.kt (Phase 4).
 * Keeping this as an interface allows unit-testing of ViewModels without
 * touching Android AlarmManager.
 */
interface AlarmScheduler {

    /**
     * Schedule (or reschedule) an alarm to fire at [alarm.wakeTime].
     * Uses AlarmManager.setAlarmClock() — survives Doze mode.
     */
    fun schedule(alarm: Alarm)

    /**
     * Cancel a scheduled alarm.
     * No-op if the alarm is not currently scheduled.
     */
    fun cancel(alarm: Alarm)
}
