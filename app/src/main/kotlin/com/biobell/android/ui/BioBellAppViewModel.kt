package com.biobell.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biobell.android.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class BioBellAppViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
) : ViewModel() {

    val uiState: StateFlow<BioBellAppUiState> = settingsRepository
        .isOnboardingComplete()
        .map { complete -> BioBellAppUiState(isOnboardingComplete = complete, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = BioBellAppUiState(isLoading = true),
        )
}

data class BioBellAppUiState(
    val isOnboardingComplete: Boolean = false,
    val isLoading: Boolean = true,
)
