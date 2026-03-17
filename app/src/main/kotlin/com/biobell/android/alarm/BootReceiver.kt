package com.biobell.android.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.biobell.android.domain.repository.AlarmRepository
import com.biobell.android.domain.repository.AlarmScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * BroadcastReceiver for BOOT_COMPLETED and QUICKBOOT_POWERON.
 *
 * AlarmManager does NOT survive device reboots. This receiver fires
 * immediately after boot and reschedules all enabled alarms from Room.
 *
 * Without this:
 * - Device reboots at 2am
 * - User's 7:00am alarm is silently gone
 * - They wake up late
 *
 * With this:
 * - Boot fires → we load ALL enabled alarms from Room → reschedule each
 *
 * @HiltAndroidApp in BioBellApplication ensures Hilt is ready by boot time.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var alarmRepository: AlarmRepository
    @Inject lateinit var alarmScheduler: AlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != "android.intent.action.QUICKBOOT_POWERON"
        ) return

        // Use goAsync() to extend the BroadcastReceiver's lifecycle for the coroutine
        val pendingResult = goAsync()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        scope.launch {
            try {
                val enabledAlarms = alarmRepository.getEnabledAlarms()
                enabledAlarms.forEach { alarm ->
                    alarmScheduler.schedule(alarm)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
