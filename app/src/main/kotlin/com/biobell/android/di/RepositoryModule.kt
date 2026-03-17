package com.biobell.android.di

import com.biobell.android.data.repository.AlarmRepositoryImpl
import com.biobell.android.data.repository.SettingsRepositoryImpl
import com.biobell.android.domain.repository.AlarmRepository
import com.biobell.android.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module binding repository interfaces to their implementations.
 *
 * Uses @Binds (more efficient than @Provides for interface bindings).
 * Installed in [SingletonComponent] — single instances shared across the app.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAlarmRepository(
        impl: AlarmRepositoryImpl,
    ): AlarmRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        impl: SettingsRepositoryImpl,
    ): SettingsRepository
}
