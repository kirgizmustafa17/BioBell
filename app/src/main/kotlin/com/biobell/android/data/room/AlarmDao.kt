package com.biobell.android.data.room

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the alarms table.
 *
 * All queries return [Flow] for reactive UI updates, except one-shot lookups
 * and write operations which are suspend functions.
 */
@Dao
interface AlarmDao {

    /** Stream of all alarms, ordered by wake time (soonest first). */
    @Query("SELECT * FROM alarms ORDER BY wakeTimeEpochMilli ASC")
    fun getAllAlarms(): Flow<List<AlarmEntity>>

    /** Get a single alarm by ID, or null. */
    @Query("SELECT * FROM alarms WHERE id = :id LIMIT 1")
    suspend fun getAlarmById(id: Long): AlarmEntity?

    /** Get all enabled alarms (used by BootReceiver to reschedule after reboot). */
    @Query("SELECT * FROM alarms WHERE isEnabled = 1")
    suspend fun getEnabledAlarms(): List<AlarmEntity>

    /**
     * Insert or replace an alarm. Returns the row ID (= alarm ID).
     * Use this for both create and update-all-fields operations.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(alarm: AlarmEntity): Long

    /** Update specific fields of an existing alarm. */
    @Update
    suspend fun update(alarm: AlarmEntity)

    /** Delete an alarm by its entity. */
    @Delete
    suspend fun delete(alarm: AlarmEntity)

    /** Delete an alarm by ID. Returns number of rows deleted. */
    @Query("DELETE FROM alarms WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    /** Delete all alarms (for testing / debug reset). */
    @Query("DELETE FROM alarms")
    suspend fun deleteAll()
}
