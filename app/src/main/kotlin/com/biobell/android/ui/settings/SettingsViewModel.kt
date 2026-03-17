package com.biobell.android.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biobell.android.domain.model.Chronotype
import com.biobell.android.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Settings screen.
 *
 * Reads from and writes to [SettingsRepository] (DataStore-backed).
 * Exposes a single immutable [StateFlow<SettingsUiState>].
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.getChronotype(),
        settingsRepository.use24HourFormat(),
    ) { chronotype, use24h ->
        SettingsUiState(
            chronotype = chronotype,
            use24HourFormat = use24h,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(),
    )

    fun onChronotypeSelected(chronotype: Chronotype) {
        viewModelScope.launch {
            settingsRepository.setChronotype(chronotype)
        }
    }

    fun onUse24HourFormatToggled(use24h: Boolean) {
        viewModelScope.launch {
            settingsRepository.setUse24HourFormat(use24h)
        }
    }
}

/** Immutable UI state for the Settings screen. */
data class SettingsUiState(
    val chronotype: Chronotype = Chronotype.INTERMEDIATE,
    val use24HourFormat: Boolean = false,
)
