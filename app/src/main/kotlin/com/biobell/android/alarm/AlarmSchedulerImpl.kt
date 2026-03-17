package com.biobell.android.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
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
 * ## Why NOT setExact()?
 * - setExact() is throttled/deferred in Doze mode (Android 6+)
 * - Can miss the scheduled time by minutes or even hours on aggressive OEMs
 *
 * ## PendingIntent flags
 * Always FLAG_IMMUTABLE (required API 31+) + FLAG_UPDATE_CURRENT to update existing intents.
 */
@Singleton
class AlarmSchedulerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : AlarmScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun schedule(alarm: Alarm) {
        if (!alarm.isEnabled) return

        val triggerAtMillis = alarm.wakeTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        // Skip if the alarm time is already in the past (> 1 second ago)
        if (triggerAtMillis < System.currentTimeMillis() - 1_000) return

        val alarmIntent = buildAlarmPendingIntent(alarm.id)

        // AlarmClockInfo shows the alarm time in the system status bar
        val alarmClockInfo = AlarmManager.AlarmClockInfo(
            triggerAtMillis,
            buildShowAlarmPendingIntent(alarm.id),  // Intent to open app when clock tapped
        )

        alarmManager.setAlarmClock(alarmClockInfo, alarmIntent)
    }

    override fun cancel(alarm: Alarm) {
        val alarmIntent = buildAlarmPendingIntent(alarm.id)
        alarmManager.cancel(alarmIntent)
    }

    /**
     * Build the [PendingIntent] that fires when the alarm triggers.
     * This is received by [AlarmReceiver].
     */
    private fun buildAlarmPendingIntent(alarmId: Long): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_ALARM_TRIGGERED
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
        }
        return PendingIntent.getBroadcast(
            context,
            alarmId.toInt(),   // requestCode = alarmId for uniqueness
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    /**
     * Build the show intent shown in the AlarmClockInfo —
     * opens the app when the user taps the clock icon in the status bar.
     */
    private fun buildShowAlarmPendingIntent(alarmId: Long): PendingIntent {
        val intent = Intent(context, Class.forName("com.biobell.android.MainActivity")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
        }
        return PendingIntent.getActivity(
            context,
            (alarmId + 100_000).toInt(),  // offset to avoid collision with alarm PendingIntents
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }
}
