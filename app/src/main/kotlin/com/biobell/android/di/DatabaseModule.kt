package com.biobell.android.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import com.biobell.android.data.datastore.userPreferencesDataStore
import com.biobell.android.data.room.AlarmDao
import com.biobell.android.data.room.AlarmDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing the Room database and its DAOs.
 *
 * Installed in [SingletonComponent] — one instance for the entire app lifetime.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAlarmDatabase(
        @ApplicationContext context: Context,
    ): AlarmDatabase = Room.databaseBuilder(
        context,
        AlarmDatabase::class.java,
        AlarmDatabase.DATABASE_NAME,
    )
        // Schema migrations: add explicit Migration objects before removing this in production
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    @Singleton
    fun provideAlarmDao(database: AlarmDatabase): AlarmDao =
        database.alarmDao()

    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = context.userPreferencesDataStore
}
