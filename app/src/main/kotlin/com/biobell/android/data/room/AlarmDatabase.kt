package com.biobell.android.data.room

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * BioBell Room database.
 *
 * Single database with one entity for v1.
 *
 * Migration strategy:
 * - Debug: fallbackToDestructiveMigration (acceptable during development)
 * - Release: explicit migration objects added per schema version bump
 */
@Database(
    entities = [AlarmEntity::class],
    version = 1,
    exportSchema = true,  // schema exported to app/schemas/ for migration validation
)
abstract class AlarmDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao

    companion object {
        const val DATABASE_NAME = "biobell_alarms.db"
    }
}
