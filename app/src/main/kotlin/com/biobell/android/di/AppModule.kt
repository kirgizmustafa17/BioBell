package com.biobell.android.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Main application-scoped Hilt module.
 * Dependencies provided here are available throughout the app lifetime.
 * Populated in subsequent phases (DatabaseModule, RepositoryModule, etc.).
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    // Phase 3: Room database and DataStore dependencies will be added here
}
