package com.biobell.android.domain.repository

import com.biobell.android.domain.model.Alarm
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for alarm persistence.
 *
 * The implementation lives in data/repository/AlarmRepositoryImpl.kt (Phase 3).
 * All UI and domain code depends only on this interface — not the implementation.
 */
interface AlarmRepository {

    /** Stream of all alarms, updated reactively on any change. */
    fun getAllAlarms(): Flow<List<Alarm>>

    /** Get a single alarm by ID, or null if not found. */
    suspend fun getAlarmById(id: Long): Alarm?

    /**
     * Insert a new alarm and return its generated ID.
     * If an alarm with this ID already exists it will be replaced.
     */
    suspend fun insertAlarm(alarm: Alarm): Long

    /** Update an existing alarm. No-op if alarm does not exist. */
    suspend fun updateAlarm(alarm: Alarm)

    /** Delete an alarm by ID. No-op if alarm does not exist. */
    suspend fun deleteAlarm(id: Long)

    /** Delete a specific alarm instance. */
    suspend fun deleteAlarm(alarm: Alarm)

    /** Get only enabled alarms (for rescheduling after reboot). */
    suspend fun getEnabledAlarms(): List<Alarm>
}
