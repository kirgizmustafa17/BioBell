package com.biobell.android.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

/**
 * BroadcastReceiver that handles alarm trigger events from [AlarmSchedulerImpl].
 *
 * Three actions:
 * - [ACTION_ALARM_TRIGGERED] : AlarmManager fired — start the foreground service
 * - [ACTION_DISMISS]         : User dismissed — stop service, optionally reschedule repeating
 * - [ACTION_SNOOZE]          : User snoozed — stop service, schedule snooze wake-up
 *
 * NOTE: NOT annotated with @AndroidEntryPoint — this receiver does not inject
 * any Hilt dependencies. Removing the annotation reduces Hilt-init overhead
 * when the process is freshly started by the alarm (e.g. after MIUI kills the app).
 *
 * IMPORTANT: Receivers must complete quickly (<10s). All heavy work
 * (audio, notifications) is delegated to [AlarmForegroundService].
 */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)
        Log.d(TAG, "onReceive: action=${intent.action} alarmId=$alarmId")

        if (alarmId == -1L) {
            Log.w(TAG, "Received alarm intent with no alarm ID — ignoring")
            return
        }

        val serviceIntent = Intent(context, AlarmForegroundService::class.java).apply {
            action = intent.action
            putExtra(EXTRA_ALARM_ID, alarmId)
        }

        when (intent.action) {
            ACTION_ALARM_TRIGGERED -> {
                Log.d(TAG, "Starting foreground service for alarm $alarmId")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }
            ACTION_DISMISS, ACTION_SNOOZE -> {
                context.startService(serviceIntent)
            }
            else -> Log.w(TAG, "Unknown action: ${intent.action}")
        }
    }

    companion object {
        private const val TAG = "AlarmReceiver"
        const val ACTION_ALARM_TRIGGERED = "com.biobell.android.ACTION_ALARM_TRIGGERED"
        const val ACTION_DISMISS         = "com.biobell.android.ACTION_DISMISS"
        const val ACTION_SNOOZE          = "com.biobell.android.ACTION_SNOOZE"
        const val EXTRA_ALARM_ID         = "extra_alarm_id"
    }
}
