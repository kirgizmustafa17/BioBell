package com.biobell.android.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.biobell.android.domain.repository.AlarmRepository
import com.biobell.android.domain.repository.AlarmScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

/**
 * Foreground service that handles a ringing alarm:
 * - Plays the alarm ringtone via [MediaPlayer]
 * - Vibrates the device
 * - Shows a persistent full-screen notification (shows on lockscreen)
 * - Handles dismiss and snooze actions
 *
 * The service acquires a WakeLock to prevent the CPU from sleeping
 * before the notification can display.
 */
@AndroidEntryPoint
class AlarmForegroundService : Service() {

    @Inject lateinit var alarmRepository: AlarmRepository
    @Inject lateinit var alarmScheduler: AlarmScheduler

    private var mediaPlayer: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var vibrator: Vibrator? = null

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val alarmId = intent?.getLongExtra(AlarmReceiver.EXTRA_ALARM_ID, -1L) ?: -1L
        if (alarmId == -1L) {
            stopSelf()
            return START_NOT_STICKY
        }

        when (intent?.action) {
            AlarmReceiver.ACTION_ALARM_TRIGGERED -> handleAlarmTriggered(alarmId)
            AlarmReceiver.ACTION_DISMISS         -> handleDismiss(alarmId)
            AlarmReceiver.ACTION_SNOOZE          -> handleSnooze(alarmId)
        }

        return START_STICKY
    }

    private fun handleAlarmTriggered(alarmId: Long) {
        acquireWakeLock()
        startForeground(NOTIFICATION_ID, buildRingingNotification(alarmId))
        playRingtone(alarmId)
        startVibration()
    }

    private fun handleDismiss(alarmId: Long) {
        stopRingtone()
        stopVibration()
        serviceScope.launch {
            val alarm = alarmRepository.getAlarmById(alarmId) ?: return@launch
            // For repeating alarms: reschedule next occurrence; disable one-shot alarms
            if (alarm.isRepeating) {
                alarmScheduler.schedule(alarm)  // will schedule next day occurrence
            } else {
                alarmRepository.updateAlarm(alarm.copy(isEnabled = false))
            }
        }
        stopSelf()
    }

    private fun handleSnooze(alarmId: Long) {
        stopRingtone()
        stopVibration()
        serviceScope.launch {
            val alarm = alarmRepository.getAlarmById(alarmId) ?: return@launch
            val snoozedAlarm = alarm.copy(
                wakeTime = LocalDateTime.now().plusMinutes(alarm.snoozeDurationMinutes.toLong()),
                isEnabled = true,
            )
            alarmScheduler.schedule(snoozedAlarm)
        }
        stopSelf()
    }

    private fun playRingtone(alarmId: Long) {
        try {
            serviceScope.launch {
                val alarm = alarmRepository.getAlarmById(alarmId)
                val ringtoneUri: Uri = alarm?.ringtoneUri?.let { Uri.parse(it) }
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

                mediaPlayer?.release()
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setLegacyStreamType(AudioManager.STREAM_ALARM)
                            .build()
                    )
                    setDataSource(this@AlarmForegroundService, ringtoneUri)
                    isLooping = true
                    prepare()
                    start()
                }
            }
        } catch (e: Exception) {
            // Fallback — best effort; don't crash if audio fails
        }
    }

    private fun stopRingtone() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun startVibration() {
        val pattern = longArrayOf(0, 500, 500, 500, 500, 500)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibrator = vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }

    private fun stopVibration() {
        vibrator?.cancel()
        vibrator = null
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "BioBell:AlarmWakeLock",
        ).also {
            it.acquire(10 * 60 * 1000L)  // 10 minutes max — prevents runaway lock
        }
    }

    private fun buildRingingNotification(alarmId: Long): Notification {
        val fullScreenIntent = Intent(this, Class.forName("com.biobell.android.MainActivity")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this, alarmId.toInt(), fullScreenIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val dismissIntent = Intent(this, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_DISMISS
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            this, (alarmId + 200_000).toInt(), dismissIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val snoozeIntent = Intent(this, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_SNOOZE
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            this, (alarmId + 300_000).toInt(), snoozeIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("BioBell")
            .setContentText("Your alarm is ringing!")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Dismiss", dismissPendingIntent)
            .addAction(android.R.drawable.ic_popup_reminder, "Snooze 9 min", snoozePendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Alarm",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "BioBell alarm notifications"
            setBypassDnd(true)
            setShowBadge(false)
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        stopRingtone()
        stopVibration()
        wakeLock?.release()
        wakeLock = null
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL_ID = "biobell_alarm_channel"
        private const val NOTIFICATION_ID = 1
    }
}
