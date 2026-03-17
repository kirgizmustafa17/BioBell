package com.biobell.android.domain.repository

import com.biobell.android.domain.model.Chronotype
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for user preferences and settings.
 *
 * The implementation lives in data/repository/SettingsRepositoryImpl.kt (Phase 3).
 */
interface SettingsRepository {

    /** Stream of the user's current chronotype. Emits on change. */
    fun getChronotype(): Flow<Chronotype>

    /** Update the user's chronotype preference. */
    suspend fun setChronotype(chronotype: Chronotype)

    /** Whether the user has completed the first-launch onboarding flow. */
    fun isOnboardingComplete(): Flow<Boolean>

    /** Mark onboarding as complete. */
    suspend fun setOnboardingComplete()

    /** Whether the battery optimization prompt has been shown. */
    suspend fun wasBatteryOptimizationPromptShown(): Boolean

    /** Mark battery optimization prompt as shown (never shown again). */
    suspend fun setBatteryOptimizationPromptShown()

    /** Whether to use 24h time format (vs 12h). Falls back to system default. */
    fun use24HourFormat(): Flow<Boolean>

    /** Override the time format preference. */
    suspend fun setUse24HourFormat(use24h: Boolean)
}
