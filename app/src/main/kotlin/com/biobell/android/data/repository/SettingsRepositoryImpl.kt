package com.biobell.android.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.biobell.android.data.datastore.PreferenceKeys
import com.biobell.android.domain.model.Chronotype
import com.biobell.android.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Production implementation of [SettingsRepository] backed by Jetpack DataStore.
 *
 * Injected by Hilt via [RepositoryModule]. All callers depend only on [SettingsRepository].
 */
@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    override fun getChronotype(): Flow<Chronotype> =
        dataStore.data.map { prefs ->
            val name = prefs[PreferenceKeys.CHRONOTYPE]
            runCatching { Chronotype.valueOf(name ?: "") }.getOrDefault(Chronotype.INTERMEDIATE)
        }

    override suspend fun setChronotype(chronotype: Chronotype) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.CHRONOTYPE] = chronotype.name
        }
    }

    override fun isOnboardingComplete(): Flow<Boolean> =
        dataStore.data.map { prefs ->
            prefs[PreferenceKeys.ONBOARDING_COMPLETE] ?: false
        }

    override suspend fun setOnboardingComplete() {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.ONBOARDING_COMPLETE] = true
        }
    }

    override suspend fun wasBatteryOptimizationPromptShown(): Boolean =
        dataStore.data.first()[PreferenceKeys.BATTERY_OPTIMIZATION_PROMPT_SHOWN] ?: false

    override suspend fun setBatteryOptimizationPromptShown() {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.BATTERY_OPTIMIZATION_PROMPT_SHOWN] = true
        }
    }

    override fun use24HourFormat(): Flow<Boolean> =
        dataStore.data.map { prefs ->
            prefs[PreferenceKeys.USE_24_HOUR_FORMAT] ?: false
        }

    override suspend fun setUse24HourFormat(use24h: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferenceKeys.USE_24_HOUR_FORMAT] = use24h
        }
    }
}
