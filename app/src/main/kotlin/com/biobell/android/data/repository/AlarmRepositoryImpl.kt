package com.biobell.android.data.repository

import com.biobell.android.data.room.AlarmDao
import com.biobell.android.data.room.AlarmMapper
import com.biobell.android.domain.model.Alarm
import com.biobell.android.domain.repository.AlarmRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Production implementation of [AlarmRepository] backed by Room.
 *
 * Injected by Hilt via [DatabaseModule]. All callers depend only
 * on the [AlarmRepository] interface — not this class directly.
 */
@Singleton
class AlarmRepositoryImpl @Inject constructor(
    private val dao: AlarmDao,
) : AlarmRepository {

    override fun getAllAlarms(): Flow<List<Alarm>> =
        dao.getAllAlarms().map { entities -> entities.map(AlarmMapper::toDomain) }

    override suspend fun getAlarmById(id: Long): Alarm? =
        dao.getAlarmById(id)?.let(AlarmMapper::toDomain)

    override suspend fun insertAlarm(alarm: Alarm): Long =
        dao.insertOrReplace(AlarmMapper.toEntity(alarm))

    override suspend fun updateAlarm(alarm: Alarm) {
        dao.update(AlarmMapper.toEntity(alarm))
    }

    override suspend fun deleteAlarm(id: Long) {
        dao.deleteById(id)
    }

    override suspend fun deleteAlarm(alarm: Alarm) {
        dao.delete(AlarmMapper.toEntity(alarm))
    }

    override suspend fun getEnabledAlarms(): List<Alarm> =
        dao.getEnabledAlarms().map(AlarmMapper::toDomain)
}
