package com.biobell.android.domain.engine

import com.biobell.android.domain.model.Chronotype
import com.biobell.android.domain.model.HealthScore
import com.biobell.android.domain.model.SleepPlan
import com.biobell.android.domain.model.SleepWarning
import com.biobell.android.domain.model.SleepWarning.WarningCode
import com.biobell.android.domain.model.WarningSeverity
import com.biobell.android.domain.engine.SleepConstants.CYCLE_DURATION_MINUTES
import com.biobell.android.domain.engine.SleepConstants.MAX_SUGGESTION_CYCLES
import com.biobell.android.domain.engine.SleepConstants.MIN_HEALTHY_DURATION_MINUTES
import com.biobell.android.domain.engine.SleepConstants.MIN_SUGGESTION_CYCLES
import com.biobell.android.domain.engine.SleepConstants.RECOMMENDED_DURATION_MINUTES
import com.biobell.android.domain.engine.SleepConstants.SLEEP_ONSET_MINUTES
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Core sleep science calculation engine for BioBell.
 *
 * Pure Kotlin — zero Android dependencies. All methods are deterministic and testable.
 *
 * ## Key concepts
 * - **Sleep onset offset**: 15 minutes subtracted from time in bed before any sleep occurs
 * - **Sleep cycle**: 90 minutes. Waking at the end of a cycle = lighter, easier waking
 * - **Chronotype offset**: shifts suggestions ±30 min based on the user's biological clock
 *
 * ## Important: always use LocalDateTime, not LocalTime
 * Sleep math crosses midnight (bedtime 23:00 → wake 07:00 spans two calendar dates).
 * LocalTime subtraction wraps incorrectly — always use LocalDateTime for delta calculations.
 */
object SleepMathEngine {

    // ─────────────────────────────────────────────────────────────────────────
    // PUBLIC API
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Given a target [wakeTime] and [chronotype], return a list of suggested bedtimes.
     *
     * Suggestions are ordered from most to least recommended:
     * - 5 cycles (recommended, 7h30m net sleep)
     * - 4 cycles (minimum healthy, 6h net sleep)
     * - 6 cycles (extended, 9h net sleep)
     * - 3 cycles (flagged as too short, 4h30m net sleep)
     *
     * Each returned [SleepPlan] is fully resolved with score and warnings.
     *
     * @param wakeTime  Target wake-up time (date portion is today or next applicable day)
     * @param chronotype  User's chronotype — shifts suggestions by its [Chronotype.offsetMinutes]
     */
    fun suggestBedtimes(
        wakeTime: LocalDateTime,
        chronotype: Chronotype,
    ): List<SleepPlan> {
        val adjustedWake = applyChronotype(wakeTime, chronotype)
        return (MIN_SUGGESTION_CYCLES..MAX_SUGGESTION_CYCLES)
            .map { cycles ->
                val totalMinutesInBed = cycles * CYCLE_DURATION_MINUTES + SLEEP_ONSET_MINUTES
                val bedtime = adjustedWake.minusMinutes(totalMinutesInBed.toLong())
                buildSleepPlan(bedtime, adjustedWake, cycles, chronotype)
            }
            .sortedByDescending { it.healthScore.score }
    }

    /**
     * Given a target [bedtime] and [chronotype], return a list of suggested wake times.
     *
     * Suggestions are ordered from most to least recommended (same cycle range).
     *
     * @param bedtime   When the user plans to go to bed
     * @param chronotype  User's chronotype
     */
    fun suggestWakeTimes(
        bedtime: LocalDateTime,
        chronotype: Chronotype,
    ): List<SleepPlan> {
        val adjustedBedtime = applyChronotype(bedtime, chronotype)
        val sleepStart = adjustedBedtime.plusMinutes(SLEEP_ONSET_MINUTES.toLong())
        return (MIN_SUGGESTION_CYCLES..MAX_SUGGESTION_CYCLES)
            .map { cycles ->
                val wakeTime = sleepStart.plusMinutes((cycles * CYCLE_DURATION_MINUTES).toLong())
                buildSleepPlan(adjustedBedtime, wakeTime, cycles, chronotype)
            }
            .sortedByDescending { it.healthScore.score }
    }

    /**
     * Validate the combination of [bedtime] + [wakeTime] and return any [SleepWarning]s.
     *
     * Does NOT modify the times — just evaluates the plan as-is.
     *
     * @param bedtime     When the user plans to go to bed
     * @param wakeTime    When the user wants to wake up
     * @param chronotype  Used to detect chronotype mismatch warnings
     */
    fun validateSleepPlan(
        bedtime: LocalDateTime,
        wakeTime: LocalDateTime,
        chronotype: Chronotype,
    ): List<SleepWarning> {
        val durationMinutes = minutesBetween(bedtime, wakeTime) - SLEEP_ONSET_MINUTES
        val cycles = durationMinutes / CYCLE_DURATION_MINUTES
        val remainder = durationMinutes % CYCLE_DURATION_MINUTES
        return buildWarnings(durationMinutes, cycles, remainder, wakeTime, chronotype)
    }

    /**
     * Score the health of a sleep plan on a 0–100 scale.
     *
     * Scoring factors:
     * - Duration (is it ≥ recommended 7h30m?)
     * - Cycle alignment (waking at end of a cycle?)
     * - Chronotype alignment
     *
     * @param durationMinutes  Net sleep duration (already minus onset offset)
     * @param wakeTime         Wake time for chronotype alignment check
     * @param chronotype       User's chronotype
     */
    fun scoreHealth(
        durationMinutes: Int,
        wakeTime: LocalDateTime,
        chronotype: Chronotype,
    ): HealthScore {
        val cycles = durationMinutes / CYCLE_DURATION_MINUTES
        val remainder = durationMinutes % CYCLE_DURATION_MINUTES

        // Base score from duration
        var score = when {
            durationMinutes >= RECOMMENDED_DURATION_MINUTES -> 90   // ≥ 7h30m — excellent base
            durationMinutes >= MIN_HEALTHY_DURATION_MINUTES -> 70   // 6h–7h30m — fair
            durationMinutes >= (MIN_SUGGESTION_CYCLES * CYCLE_DURATION_MINUTES) -> 40  // 4h30m–6h
            else -> 10
        }

        // Bonus for on-cycle wake (remainder within 10 min of boundary)
        if (remainder <= 10 || remainder >= CYCLE_DURATION_MINUTES - 10) {
            score = (score + 10).coerceAtMost(100)
        } else {
            // Penalty for mid-cycle wake
            score = (score - 5).coerceAtLeast(0)
        }

        // Bonus for extra cycles beyond minimum
        if (cycles >= 5) score = (score + 5).coerceAtMost(100)

        val summary = buildSummary(durationMinutes, cycles)
        return HealthScore.fromScore(score, summary)
    }

    /**
     * Apply a chronotype offset to a [LocalDateTime].
     *
     * Night owl → times shift later (+30 min)
     * Early bird → times shift earlier (−30 min)
     * Intermediate → no shift
     *
     * @param time        Base time (bedtime or wake time)
     * @param chronotype  User's chronotype
     */
    fun applyChronotype(time: LocalDateTime, chronotype: Chronotype): LocalDateTime =
        time.plusMinutes(chronotype.offsetMinutes.toLong())

    /**
     * Build a complete [SleepPlan] from a resolved [bedtime] and [wakeTime].
     * Useful for re-evaluating an existing alarm without re-running suggestion logic.
     */
    fun buildPlanFromTimes(
        bedtime: LocalDateTime,
        wakeTime: LocalDateTime,
        chronotype: Chronotype,
    ): SleepPlan {
        val durationMinutes = (minutesBetween(bedtime, wakeTime) - SLEEP_ONSET_MINUTES)
            .coerceAtLeast(0)
        val cycles = durationMinutes / CYCLE_DURATION_MINUTES
        return buildSleepPlan(bedtime, wakeTime, cycles, chronotype)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // INTERNAL HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    internal fun buildSleepPlan(
        bedtime: LocalDateTime,
        wakeTime: LocalDateTime,
        cycles: Int,
        chronotype: Chronotype,
    ): SleepPlan {
        val durationMinutes = (minutesBetween(bedtime, wakeTime) - SLEEP_ONSET_MINUTES)
            .coerceAtLeast(0)
        val remainder = durationMinutes % CYCLE_DURATION_MINUTES
        val warnings = buildWarnings(durationMinutes, cycles, remainder, wakeTime, chronotype)
        val healthScore = scoreHealth(durationMinutes, wakeTime, chronotype)
        return SleepPlan(
            bedtime = bedtime,
            wakeTime = wakeTime,
            durationMinutes = durationMinutes,
            cycles = cycles,
            chronotype = chronotype,
            healthScore = healthScore,
            warnings = warnings,
        )
    }

    internal fun buildWarnings(
        durationMinutes: Int,
        cycles: Int,
        remainder: Int,
        wakeTime: LocalDateTime,
        chronotype: Chronotype,
    ): List<SleepWarning> {
        val warnings = mutableListOf<SleepWarning>()

        // Duration warnings
        when {
            durationMinutes < MIN_SUGGESTION_CYCLES * CYCLE_DURATION_MINUTES ->
                warnings += SleepWarning(
                    code = WarningCode.DURATION_BELOW_MINIMUM,
                    severity = WarningSeverity.ERROR,
                    message = "Only ${formatDuration(durationMinutes)} planned — below the 6h minimum for healthy sleep.",
                )
            durationMinutes < MIN_HEALTHY_DURATION_MINUTES ->
                warnings += SleepWarning(
                    code = WarningCode.DURATION_BELOW_MINIMUM,
                    severity = WarningSeverity.ERROR,
                    message = "${formatDuration(durationMinutes)} is below the recommended 6h minimum.",
                )
            durationMinutes < RECOMMENDED_DURATION_MINUTES ->
                warnings += SleepWarning(
                    code = WarningCode.DURATION_BELOW_RECOMMENDED,
                    severity = WarningSeverity.WARNING,
                    message = "${formatDuration(durationMinutes)} planned — slightly below the recommended 7h30m.",
                )
        }

        // Mid-cycle wake warning
        val midCycleThreshold = 15  // minutes from cycle boundary
        if (remainder > midCycleThreshold && remainder < CYCLE_DURATION_MINUTES - midCycleThreshold) {
            warnings += SleepWarning(
                code = WarningCode.MID_CYCLE_WAKE,
                severity = WarningSeverity.WARNING,
                message = "Waking mid-cycle ($remainder min into a cycle) — adjust by ${CYCLE_DURATION_MINUTES - remainder} min for an easier wake.",
            )
        }

        // Chronotype mismatch — simplified check
        val wakeHour = wakeTime.hour
        val chronotypeMismatch = when (chronotype) {
            Chronotype.EARLY_BIRD -> wakeHour > 8   // Early birds waking after 8am
            Chronotype.NIGHT_OWL  -> wakeHour in 5..6  // Night owls forced to wake very early
            Chronotype.INTERMEDIATE -> false
        }
        if (chronotypeMismatch) {
            warnings += SleepWarning(
                code = WarningCode.CHRONOTYPE_MISMATCH,
                severity = WarningSeverity.INFO,
                message = "This wake time doesn't align well with your ${chronotype.label} chronotype.",
            )
        }

        return warnings
    }

    /**
     * Calculate whole minutes between two [LocalDateTime]s.
     * Always returns a positive value — if [end] is before [start], wraps to next day.
     */
    internal fun minutesBetween(start: LocalDateTime, end: LocalDateTime): Int {
        var e = end
        // Handle midnight crossover: if end is "before" start on the clock,
        // end is actually on the next day
        if (e <= start) {
            e = e.plusDays(1)
        }
        return java.time.Duration.between(start, e).toMinutes().toInt()
    }

    internal fun formatDuration(minutes: Int): String {
        val h = minutes / 60
        val m = minutes % 60
        return if (m == 0) "${h}h" else "${h}h${m}m"
    }

    internal fun buildSummary(durationMinutes: Int, cycles: Int): String {
        val durationStr = formatDuration(durationMinutes)
        return when {
            cycles >= 5 && durationMinutes >= RECOMMENDED_DURATION_MINUTES ->
                "Great — $cycles full cycles ($durationStr)"
            cycles >= 4 ->
                "Good — $cycles cycles ($durationStr)"
            cycles == 3 ->
                "Short — $cycles cycles ($durationStr)"
            else ->
                "Very short — $durationStr"
        }
    }
}
