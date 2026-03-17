package com.biobell.android.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

/** Extension property — creates a single DataStore instance per Context. */
val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_preferences",
)

/**
 * Keys for all values stored in DataStore.
 * Kept in one place to avoid duplication and typos.
 */
object PreferenceKeys {
    val CHRONOTYPE = stringPreferencesKey("chronotype")
    val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
    val BATTERY_OPTIMIZATION_PROMPT_SHOWN = booleanPreferencesKey("battery_opt_prompt_shown")
    val USE_24_HOUR_FORMAT = booleanPreferencesKey("use_24_hour_format")
}
