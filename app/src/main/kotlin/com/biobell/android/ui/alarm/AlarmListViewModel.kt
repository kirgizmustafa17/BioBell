package com.biobell.android.ui.alarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biobell.android.domain.model.Alarm
import com.biobell.android.domain.repository.AlarmRepository
import com.biobell.android.domain.repository.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the AlarmList screen.
 *
 * Exposes the live list of alarms as [StateFlow] for the UI.
 * Handles toggle/delete/schedule operations via the repository + scheduler.
 */
@HiltViewModel
class AlarmListViewModel @Inject constructor(
    private val alarmRepository: AlarmRepository,
    private val alarmScheduler: AlarmScheduler,
) : ViewModel() {

    /** Live alarm list, collected from Room via Flow → StateFlow. */
    val uiState: StateFlow<AlarmListUiState> = alarmRepository
        .getAllAlarms()
        .map { alarms -> AlarmListUiState(alarms = alarms, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AlarmListUiState(isLoading = true),
        )

    /** Toggle an alarm on or off. Reschedules or cancels as needed. */
    fun onToggleAlarm(alarm: Alarm, enabled: Boolean) {
        viewModelScope.launch {
            val updated = alarm.copy(isEnabled = enabled)
            alarmRepository.updateAlarm(updated)
            if (enabled) {
                alarmScheduler.schedule(updated)
            } else {
                alarmScheduler.cancel(updated)
            }
        }
    }

    /** Delete an alarm permanently. Cancels any pending schedule. */
    fun onDeleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            alarmScheduler.cancel(alarm)
            alarmRepository.deleteAlarm(alarm)
        }
    }
}

/** Immutable UI state for [AlarmListViewModel]. */
data class AlarmListUiState(
    val alarms: List<Alarm> = emptyList(),
    val isLoading: Boolean = false,
) {
    val isEmpty: Boolean get() = !isLoading && alarms.isEmpty()
}
