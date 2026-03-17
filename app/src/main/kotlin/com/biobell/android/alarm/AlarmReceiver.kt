package com.biobell.android.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.AndroidEntryPoint

/**
 * BroadcastReceiver that handles alarm trigger events from [AlarmSchedulerImpl].
 *
 * Three actions:
 * - [ACTION_ALARM_TRIGGERED] : AlarmManager fired — start the foreground service
 * - [ACTION_DISMISS]         : User dismissed — stop service, optionally reschedule repeating
 * - [ACTION_SNOOZE]          : User snoozed — stop service, schedule snooze wake-up
 *
 * IMPORTANT: Receivers must complete quickly (<10s). All heavy work
 * (audio, notifications) is delegated to [AlarmForegroundService].
 */
@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)
        if (alarmId == -1L) return

        val serviceIntent = Intent(context, AlarmForegroundService::class.java).apply {
            action = intent.action
            putExtra(EXTRA_ALARM_ID, alarmId)
        }

        when (intent.action) {
            ACTION_ALARM_TRIGGERED -> {
                // Start foreground service to play ringtone and show notification
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }
            ACTION_DISMISS, ACTION_SNOOZE -> {
                // Delegate to service to handle cleanup/rescheduling
                context.startService(serviceIntent)
            }
        }
    }

    companion object {
        const val ACTION_ALARM_TRIGGERED = "com.biobell.android.ACTION_ALARM_TRIGGERED"
        const val ACTION_DISMISS         = "com.biobell.android.ACTION_DISMISS"
        const val ACTION_SNOOZE          = "com.biobell.android.ACTION_SNOOZE"
        const val EXTRA_ALARM_ID         = "extra_alarm_id"
    }
}
