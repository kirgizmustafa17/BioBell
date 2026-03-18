package com.biobell.android.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.biobell.android.MainActivity
import com.biobell.android.domain.model.Alarm
import com.biobell.android.domain.repository.AlarmScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Production implementation of [AlarmScheduler] using [AlarmManager.setAlarmClock].
 *
 * ## Why setAlarmClock()?
 * - Exempt from Doze mode — fires even when the device is idle
 * - Shows in the system clock UI and Status Bar (clock icon)
 * - Most reliable scheduling API for user-facing alarms
 *
 * ## Exact alarm permission
 * - API 33+: USE_EXACT_ALARM (auto-granted for alarm clock apps; declared in manifest)
 * - API 31-32: SCHEDULE_EXACT_ALARM (requires user grant via Settings)
 *   We guard with canScheduleExactAlarms() and log a warning if unavailable.
 */
@Singleton
class AlarmSchedulerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : AlarmScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun schedule(alarm: Alarm) {
        if (!alarm.isEnabled) return

        // Guard: exact alarm permission required on API 31+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !alarmManager.canScheduleExactAlarms()
        ) {
            Log.w(TAG, "Cannot schedule exact alarm — permission not granted for alarm id=${alarm.id}")
            return
        }

        val triggerAtMillis = alarm.wakeTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        // Skip if the alarm time is already in the past (> 1 second ago)
        if (triggerAtMillis < System.currentTimeMillis() - 1_000) {
            Log.d(TAG, "Alarm id=${alarm.id} is in the past — skipping schedule")
            return
        }

        val alarmIntent = buildAlarmPendingIntent(alarm.id)

        // AlarmClockInfo shows the alarm time in the system status bar
        val alarmClockInfo = AlarmManager.AlarmClockInfo(
            triggerAtMillis,
            buildShowAlarmPendingIntent(alarm.id),
        )

        alarmManager.setAlarmClock(alarmClockInfo, alarmIntent)
        Log.d(TAG, "Scheduled alarm id=${alarm.id} for $triggerAtMillis")
    }

    override fun cancel(alarm: Alarm) {
        val alarmIntent = buildAlarmPendingIntent(alarm.id)
        alarmManager.cancel(alarmIntent)
        Log.d(TAG, "Cancelled alarm id=${alarm.id}")
    }

    /**
     * Build the [PendingIntent] that fires when the alarm triggers.
     * Received by [AlarmReceiver].
     */
    private fun buildAlarmPendingIntent(alarmId: Long): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_ALARM_TRIGGERED
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
        }
        return PendingIntent.getBroadcast(
            context,
            alarmId.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    /**
     * Build the show intent for AlarmClockInfo —
     * opens the app when the user taps the clock icon in the status bar.
     *
     * Uses MainActivity::class.java directly (no Class.forName reflection)
     * to avoid ClassNotFoundException at runtime.
     */
    private fun buildShowAlarmPendingIntent(alarmId: Long): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
        }
        return PendingIntent.getActivity(
            context,
            (alarmId + 100_000).toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    private companion object {
        private const val TAG = "AlarmScheduler"
    }
}
