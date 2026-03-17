package com.biobell.android.di

import com.biobell.android.alarm.AlarmSchedulerImpl
import com.biobell.android.domain.repository.AlarmScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module binding [AlarmScheduler] to its [AlarmSchedulerImpl] implementation.
 *
 * Separated from [RepositoryModule] so alarm scheduling can be swapped
 * independently (e.g. for testing with a fake scheduler).
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AlarmModule {

    @Binds
    @Singleton
    abstract fun bindAlarmScheduler(
        impl: AlarmSchedulerImpl,
    ): AlarmScheduler
}
