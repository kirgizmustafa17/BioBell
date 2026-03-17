package com.biobell.android.ui.alarm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biobell.android.domain.engine.SleepMathEngine
import com.biobell.android.domain.model.Alarm
import com.biobell.android.domain.model.Chronotype
import com.biobell.android.domain.model.SleepPlan
import com.biobell.android.domain.model.SleepWarning
import com.biobell.android.domain.repository.AlarmRepository
import com.biobell.android.domain.repository.AlarmScheduler
import com.biobell.android.domain.repository.SettingsRepository
import com.biobell.android.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

/**
 * ViewModel for the AlarmSetter screen.
 *
 * Manages bidirectional state:
 * - User sets wake time → engine calculates bedtime suggestions
 * - User sets bedtime → engine calculates wake time suggestions
 *
 * Persists via [AlarmRepository]. Schedules via [AlarmScheduler].
 */
@HiltViewModel
class AlarmSetterViewModel @Inject constructor(
    private val alarmRepository: AlarmRepository,
    private val alarmScheduler: AlarmScheduler,
    private val settingsRepository: SettingsRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // ─── Alarm ID from nav args (null = create, non-null = edit) ───────────
    private val alarmId: Long? = savedStateHandle
        .get<Long>(Screen.AlarmSetter.ARG_ALARM_ID)
        ?.takeIf { it != -1L }

    // ─── UI State ────────────────────────────────────────────────────────────
    private val _uiState = MutableStateFlow(AlarmSetterUiState())
    val uiState: StateFlow<AlarmSetterUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Load chronotype from settings
            val chronotype = settingsRepository.getChronotype().first()
            _uiState.update { it.copy(chronotype = chronotype) }

            // Load existing alarm if editing
            if (alarmId != null) {
                loadAlarm(alarmId)
            } else {
                // Default: wake time = tomorrow 07:00
                val defaultWake = LocalDateTime.of(
                    LocalDate.now().plusDays(1),
                    LocalTime.of(7, 0),
                )
                onWakeTimeChanged(defaultWake)
            }
        }
    }

    private suspend fun loadAlarm(id: Long) {
        val alarm = alarmRepository.getAlarmById(id) ?: return
        _uiState.update { state ->
            state.copy(
                isEditing = true,
                alarmLabel = alarm.label,
                isVibrate = alarm.isVibrate,
                snoozeDurationMinutes = alarm.snoozeDurationMinutes,
                selectedPlan = alarm.sleepPlan,
                wakeTime = alarm.wakeTime,
                inputMode = InputMode.WAKE_TIME,
            )
        }
        // Compute suggestions for the loaded alarm
        alarm.sleepPlan?.let { plan ->
            computeSuggestions(alarm.wakeTime, InputMode.WAKE_TIME)
        } ?: computeSuggestions(alarm.wakeTime, InputMode.WAKE_TIME)
    }

    // ─── User Actions ────────────────────────────────────────────────────────

    /** User changed the wake time picker. Recalculate bedtime suggestions. */
    fun onWakeTimeChanged(wakeTime: LocalDateTime) {
        _uiState.update { it.copy(wakeTime = wakeTime, inputMode = InputMode.WAKE_TIME) }
        computeSuggestions(wakeTime, InputMode.WAKE_TIME)
    }

    /** User changed the bedtime picker. Recalculate wake time suggestions. */
    fun onBedtimeChanged(bedtime: LocalDateTime) {
        _uiState.update { it.copy(bedtime = bedtime, inputMode = InputMode.BEDTIME) }
        computeSuggestions(bedtime, InputMode.BEDTIME)
    }

    /** User tapped a suggestion card — select that plan. */
    fun onPlanSelected(plan: SleepPlan) {
        _uiState.update { state ->
            state.copy(
                selectedPlan = plan,
                wakeTime = plan.wakeTime,
                bedtime = plan.bedtime,
            )
        }
    }

    /** User edited the alarm label. */
    fun onLabelChanged(label: String) {
        _uiState.update { it.copy(alarmLabel = label) }
    }

    /** User toggled vibration. */
    fun onVibrateToggled(enabled: Boolean) {
        _uiState.update { it.copy(isVibrate = enabled) }
    }

    /** User tapped Save. Persists alarm and schedules it. */
    fun onSave(onComplete: () -> Unit) {
        val state = _uiState.value
        val plan = state.selectedPlan ?: return

        viewModelScope.launch {
            val alarm = Alarm(
                id = alarmId ?: 0L,
                wakeTime = plan.wakeTime,
                label = state.alarmLabel,
                isEnabled = true,
                isVibrate = state.isVibrate,
                snoozeDurationMinutes = state.snoozeDurationMinutes,
                sleepPlan = plan,
            )
            val savedId = alarmRepository.insertAlarm(alarm)
            alarmScheduler.schedule(alarm.copy(id = savedId))
            onComplete()
        }
    }

    /** User tapped Cancel / back. */
    fun onCancel(onComplete: () -> Unit) = onComplete()

    // ─── Internal ────────────────────────────────────────────────────────────

    private fun computeSuggestions(time: LocalDateTime, mode: InputMode) {
        val chronotype = _uiState.value.chronotype
        val suggestions = when (mode) {
            InputMode.WAKE_TIME -> SleepMathEngine.suggestBedtimes(time, chronotype)
            InputMode.BEDTIME   -> SleepMathEngine.suggestWakeTimes(time, chronotype)
        }

        // Auto-select the top suggestion (best health score)
        val autoSelected = suggestions.firstOrNull()

        _uiState.update { state ->
            state.copy(
                suggestions = suggestions,
                selectedPlan = autoSelected ?: state.selectedPlan,
                bedtime = autoSelected?.bedtime ?: state.bedtime,
                wakeTime = autoSelected?.wakeTime ?: state.wakeTime,
            )
        }
    }
}

/** Which time field the user is currently editing. */
enum class InputMode { WAKE_TIME, BEDTIME }

/**
 * Immutable UI state for [AlarmSetterViewModel].
 * Rendered by [AlarmSetterScreen].
 */
data class AlarmSetterUiState(
    val isEditing: Boolean = false,
    val inputMode: InputMode = InputMode.WAKE_TIME,
    val wakeTime: LocalDateTime = LocalDateTime.now().plusDays(1).withHour(7).withMinute(0).withSecond(0).withNano(0),
    val bedtime: LocalDateTime? = null,
    val chronotype: Chronotype = Chronotype.INTERMEDIATE,
    val suggestions: List<SleepPlan> = emptyList(),
    val selectedPlan: SleepPlan? = null,
    val alarmLabel: String = "",
    val isVibrate: Boolean = true,
    val snoozeDurationMinutes: Int = 9,
    val isSaving: Boolean = false,
) {
    /** Warnings from the currently selected plan. */
    val warnings: List<SleepWarning> get() = selectedPlan?.warnings ?: emptyList()
    val hasErrors: Boolean get() = warnings.any { it.severity == com.biobell.android.domain.model.WarningSeverity.ERROR }
    val canSave: Boolean get() = selectedPlan != null && !isSaving
}
