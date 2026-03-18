package com.biobell.android.ui.setup

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biobell.android.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Permission Setup onboarding screen.
 *
 * Tracks the status of each required permission and whether
 * the user has completed (or skipped) onboarding.
 */
@HiltViewModel
class PermissionSetupViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PermissionSetupUiState())
    val uiState: StateFlow<PermissionSetupUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    /** Re-check all permission states (call after returning from Settings). */
    fun refresh() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

        val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else true

        val isBatteryExempt = powerManager.isIgnoringBatteryOptimizations(context.packageName)

        _uiState.update {
            it.copy(
                canScheduleExactAlarms = canExact,
                isBatteryOptimizationExempt = isBatteryExempt,
            )
        }
    }

    /** Update notification permission state after runtime request result. */
    fun onNotificationPermissionResult(granted: Boolean) {
        _uiState.update { it.copy(notificationsGranted = granted) }
    }

    /** Open system Settings to grant SCHEDULE_EXACT_ALARM. */
    fun openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    /** Open system Settings to disable battery optimization for this app. */
    fun openBatteryOptimizationSettings() {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    /** Mark onboarding as complete and proceed to the main app. */
    fun completeSetup(onDone: () -> Unit) {
        viewModelScope.launch {
            settingsRepository.setOnboardingComplete()
            onDone()
        }
    }
}

data class PermissionSetupUiState(
    /** POST_NOTIFICATIONS — runtime permission (API 33+). */
    val notificationsGranted: Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU,
    /** SCHEDULE_EXACT_ALARM — required for exact alarm on API 31-32. */
    val canScheduleExactAlarms: Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.S,
    /** Battery optimization exemption — critical for reliability. */
    val isBatteryOptimizationExempt: Boolean = false,
) {
    /** True when all critical permissions are in place. */
    val allCriticalGranted: Boolean
        get() = canScheduleExactAlarms && notificationsGranted
}
