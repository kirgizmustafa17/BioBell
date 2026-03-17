package com.biobell.android.data.room

import com.biobell.android.domain.model.*
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Bidirectional mapper between [AlarmEntity] (Room) and [Alarm] (domain).
 *
 * Keeps domain models free of Room annotations and Room entities free of business logic.
 */
object AlarmMapper {

    fun toEntity(alarm: Alarm): AlarmEntity {
        val zone = ZoneId.systemDefault()
        val epochMilli = alarm.wakeTime
            .atZone(zone)
            .toInstant()
            .toEpochMilli()
        val plan = alarm.sleepPlan

        return AlarmEntity(
            id = alarm.id,
            wakeTimeEpochMilli = epochMilli,
            wakeTimeHour = alarm.wakeTime.hour,
            wakeTimeMinute = alarm.wakeTime.minute,
            label = alarm.label,
            isEnabled = alarm.isEnabled,
            repeatDays = alarm.repeatDays.joinToString(",") { it.name },
            ringtoneUri = alarm.ringtoneUri,
            isVibrate = alarm.isVibrate,
            snoozeDurationMinutes = alarm.snoozeDurationMinutes,
            sleepPlanBedtimeEpoch = plan?.bedtime?.atZone(zone)?.toInstant()?.toEpochMilli(),
            sleepPlanDurationMinutes = plan?.durationMinutes,
            sleepPlanCycles = plan?.cycles,
            sleepPlanHealthScore = plan?.healthScore?.score,
            sleepPlanGrade = plan?.healthScore?.grade?.name,
            chronotype = (plan?.chronotype ?: Chronotype.INTERMEDIATE).name,
        )
    }

    fun toDomain(entity: AlarmEntity): Alarm {
        val zone = ZoneId.systemDefault()
        val wakeTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(entity.wakeTimeEpochMilli),
            zone,
        )
        val repeatDays = if (entity.repeatDays.isBlank()) emptySet()
        else entity.repeatDays.split(",").mapNotNull { runCatching { DayOfWeek.valueOf(it) }.getOrNull() }.toSet()

        val chronotype = runCatching { Chronotype.valueOf(entity.chronotype) }.getOrDefault(Chronotype.INTERMEDIATE)

        val sleepPlan = buildSleepPlanFromEntity(entity, wakeTime, chronotype, zone)

        return Alarm(
            id = entity.id,
            wakeTime = wakeTime,
            label = entity.label,
            isEnabled = entity.isEnabled,
            repeatDays = repeatDays,
            ringtoneUri = entity.ringtoneUri,
            isVibrate = entity.isVibrate,
            snoozeDurationMinutes = entity.snoozeDurationMinutes,
            sleepPlan = sleepPlan,
        )
    }

    private fun buildSleepPlanFromEntity(
        entity: AlarmEntity,
        wakeTime: LocalDateTime,
        chronotype: Chronotype,
        zone: ZoneId,
    ): SleepPlan? {
        val bedtimeEpoch = entity.sleepPlanBedtimeEpoch ?: return null
        val duration = entity.sleepPlanDurationMinutes ?: return null
        val cycles = entity.sleepPlanCycles ?: return null
        val score = entity.sleepPlanHealthScore ?: return null
        val gradeName = entity.sleepPlanGrade ?: return null

        val bedtime = LocalDateTime.ofInstant(Instant.ofEpochMilli(bedtimeEpoch), zone)
        val grade = runCatching { HealthScore.Grade.valueOf(gradeName) }.getOrDefault(HealthScore.Grade.C)
        val healthScore = HealthScore(score = score, grade = grade, summary = "")

        return SleepPlan(
            bedtime = bedtime,
            wakeTime = wakeTime,
            durationMinutes = duration,
            cycles = cycles,
            chronotype = chronotype,
            healthScore = healthScore,
            warnings = emptyList(), // Warnings are ephemeral — not persisted
        )
    }
}
