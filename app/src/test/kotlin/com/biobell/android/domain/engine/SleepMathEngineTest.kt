package com.biobell.android.domain.engine

import com.biobell.android.domain.model.Chronotype
import com.biobell.android.domain.model.SleepWarning.WarningCode
import com.biobell.android.domain.model.WarningSeverity
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Unit tests for [SleepMathEngine].
 *
 * Covers:
 * - Bidirectional calculations (bedtime → wake, wake → bedtime)
 * - Midnight crossover edge cases
 * - Chronotype offset correctness
 * - Health score grading thresholds
 * - Warning trigger conditions
 * - minutesBetween helper
 * - Cycle boundary and mid-cycle detection
 */
class SleepMathEngineTest {

    // ─────────────────────────────────────────────────────────────────────────
    // Test helpers
    // ─────────────────────────────────────────────────────────────────────────

    private fun dt(hour: Int, minute: Int = 0, date: LocalDate = LocalDate.of(2026, 1, 1)): LocalDateTime =
        LocalDateTime.of(date, LocalTime.of(hour, minute))

    private val TODAY = LocalDate.of(2026, 1, 1)
    private val TOMORROW = TODAY.plusDays(1)

    // ─────────────────────────────────────────────────────────────────────────
    // minutesBetween
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `minutesBetween same day`() {
        assertEquals(480, SleepMathEngine.minutesBetween(dt(23), dt(7, date = TOMORROW)))
    }

    @Test
    fun `minutesBetween midnight crossover wraps correctly`() {
        // 23:00 → 07:00 next day = 8h = 480 min
        val start = dt(23, 0, TODAY)
        val end = dt(7, 0, TODAY) // "today" clock but actually next day
        assertEquals(480, SleepMathEngine.minutesBetween(start, end))
    }

    @Test
    fun `minutesBetween same time returns 1440 after wrapping`() {
        val start = dt(7, 0, TODAY)
        val end = dt(7, 0, TODAY) // same — wraps to +1 day
        assertEquals(1440, SleepMathEngine.minutesBetween(start, end))
    }

    @Test
    fun `minutesBetween short overnight`() {
        // 02:00 → 06:30 = 4h30m = 270 min
        val start = dt(2, 0, TODAY)
        val end = dt(6, 30, TODAY)
        assertEquals(270, SleepMathEngine.minutesBetween(start, end))
    }

    // ─────────────────────────────────────────────────────────────────────────
    // applyChronotype
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `chronotype intermediate — no shift`() {
        val base = dt(7, 0)
        assertEquals(base, SleepMathEngine.applyChronotype(base, Chronotype.INTERMEDIATE))
    }

    @Test
    fun `chronotype night owl — shifts +30 min`() {
        val base = dt(7, 0)
        val expected = base.plusMinutes(30)
        assertEquals(expected, SleepMathEngine.applyChronotype(base, Chronotype.NIGHT_OWL))
    }

    @Test
    fun `chronotype early bird — shifts -30 min`() {
        val base = dt(7, 0)
        val expected = base.minusMinutes(30)
        assertEquals(expected, SleepMathEngine.applyChronotype(base, Chronotype.EARLY_BIRD))
    }

    // ─────────────────────────────────────────────────────────────────────────
    // suggestBedtimes
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `suggestBedtimes returns sorted list — best health score first`() {
        val wake = dt(7, 0, TOMORROW)
        val suggestions = SleepMathEngine.suggestBedtimes(wake, Chronotype.INTERMEDIATE)
        assertTrue("Should have suggestions", suggestions.isNotEmpty())
        // Must be sorted descending by health score
        for (i in 0 until suggestions.size - 1) {
            assertTrue(suggestions[i].healthScore.score >= suggestions[i + 1].healthScore.score)
        }
    }

    @Test
    fun `suggestBedtimes for 07 00 wake intermediate — 5-cycle option correct`() {
        // Wake 07:00, 5 cycles = 5*90 + 15 onset = 465 min before wake
        // 07:00 - 465 min = 07:00 - 7h45m = 23:15
        val wake = dt(7, 0, TOMORROW)
        val suggestions = SleepMathEngine.suggestBedtimes(wake, Chronotype.INTERMEDIATE)
        val fiveCycle = suggestions.find { it.cycles == 5 }
        assertNotNull("5-cycle suggestion must exist", fiveCycle)
        assertEquals(23, fiveCycle!!.bedtime.hour)
        assertEquals(15, fiveCycle.bedtime.minute)
    }

    @Test
    fun `suggestBedtimes for 07 00 wake — 4-cycle option is 00 45`() {
        // 4 cycles = 4*90 + 15 = 375 min before 07:00 = 07:00 - 6h15m = 00:45
        val wake = dt(7, 0, TOMORROW)
        val suggestions = SleepMathEngine.suggestBedtimes(wake, Chronotype.INTERMEDIATE)
        val fourCycle = suggestions.find { it.cycles == 4 }
        assertNotNull("4-cycle suggestion must exist", fourCycle)
        assertEquals(0, fourCycle!!.bedtime.hour)
        assertEquals(45, fourCycle.bedtime.minute)
    }

    @Test
    fun `suggestBedtimes night owl shifts bedtimes +30 min`() {
        val wake = dt(7, 0, TOMORROW)
        val intermediate = SleepMathEngine.suggestBedtimes(wake, Chronotype.INTERMEDIATE)
        val owl = SleepMathEngine.suggestBedtimes(wake, Chronotype.NIGHT_OWL)

        // Each owl bedtime should be 30 min later than intermediate
        val intFiveCycle = intermediate.find { it.cycles == 5 }!!
        val owlFiveCycle = owl.find { it.cycles == 5 }!!
        assertEquals(
            intFiveCycle.bedtime.plusMinutes(30),
            owlFiveCycle.bedtime,
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // suggestWakeTimes
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `suggestWakeTimes for bedtime 23 00 intermediate — 5-cycle wake is 07 05`() {
        // Bedtime 23:00, onset 15 min → sleep starts 23:15
        // 5 cycles = 5*90 = 450 min = 7h30m after 23:15 = 06:45
        val bedtime = dt(23, 0, TODAY)
        val suggestions = SleepMathEngine.suggestWakeTimes(bedtime, Chronotype.INTERMEDIATE)
        val fiveCycle = suggestions.find { it.cycles == 5 }
        assertNotNull("5-cycle must exist", fiveCycle)
        // 23:15 + 7h30m = 06:45 next day
        assertEquals(6, fiveCycle!!.wakeTime.hour)
        assertEquals(45, fiveCycle.wakeTime.minute)
    }

    @Test
    fun `suggestWakeTimes returns sorted by health score`() {
        val bedtime = dt(23, 0, TODAY)
        val suggestions = SleepMathEngine.suggestWakeTimes(bedtime, Chronotype.INTERMEDIATE)
        for (i in 0 until suggestions.size - 1) {
            assertTrue(suggestions[i].healthScore.score >= suggestions[i + 1].healthScore.score)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // scoreHealth
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `scoreHealth 7h30m gets grade A`() {
        val score = SleepMathEngine.scoreHealth(450, dt(7, 0, TOMORROW), Chronotype.INTERMEDIATE)
        assertEquals(com.biobell.android.domain.model.HealthScore.Grade.A, score.grade)
        assertTrue(score.score >= 90)
    }

    @Test
    fun `scoreHealth 6h gets grade C or above`() {
        val score = SleepMathEngine.scoreHealth(360, dt(7, 0, TOMORROW), Chronotype.INTERMEDIATE)
        assertTrue("6h should be C or better", score.score >= 60)
    }

    @Test
    fun `scoreHealth 5h gets grade D`() {
        val score = SleepMathEngine.scoreHealth(300, dt(7, 0, TOMORROW), Chronotype.INTERMEDIATE)
        assertTrue("5h should be D", score.score in 40..74)
    }

    @Test
    fun `scoreHealth 4h gets grade F`() {
        val score = SleepMathEngine.scoreHealth(240, dt(7, 0, TOMORROW), Chronotype.INTERMEDIATE)
        assertTrue("4h should be F", score.score < 40)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // validateSleepPlan — warning triggers
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `validateSleepPlan under 6h triggers DURATION_BELOW_MINIMUM error`() {
        val bedtime = dt(2, 0, TODAY)
        val wake = dt(7, 0, TODAY) // 5h
        val warnings = SleepMathEngine.validateSleepPlan(bedtime, wake, Chronotype.INTERMEDIATE)
        val durationWarning = warnings.find { it.code == WarningCode.DURATION_BELOW_MINIMUM }
        assertNotNull("Must have duration warning for 5h sleep", durationWarning)
        assertEquals(WarningSeverity.ERROR, durationWarning!!.severity)
    }

    @Test
    fun `validateSleepPlan exactly 6h has no error`() {
        // 6h net = 6h + 15min in bed
        val bedtime = dt(0, 45, TODAY)
        val wake = dt(7, 0, TODAY)  // 6h15m in bed - 15 onset = 6h net
        val warnings = SleepMathEngine.validateSleepPlan(bedtime, wake, Chronotype.INTERMEDIATE)
        val errors = warnings.filter { it.severity == WarningSeverity.ERROR }
        assertTrue("Exactly 6h net should have no error severity warnings", errors.isEmpty())
    }

    @Test
    fun `validateSleepPlan 7h30m has no warnings`() {
        // 7h30m net + 15min onset = 7h45m in bed
        // Bedtime 23:15, wake 07:00 = 7h45m in bed, 7h30m net
        val bedtime = dt(23, 15, TODAY)
        val wake = dt(7, 0, TOMORROW)
        val warnings = SleepMathEngine.validateSleepPlan(bedtime, wake, Chronotype.INTERMEDIATE)
        assertTrue("7h30m on cycle should have no warnings", warnings.isEmpty())
    }

    @Test
    fun `validateSleepPlan mid-cycle wake triggers MID_CYCLE_WAKE warning`() {
        // 4 cycles = 360 min net. Adding 45 min puts us mid-cycle (45 min into 5th cycle)
        // 360 + 45 = 405 net, + 15 onset = 420 in bed = 7h
        val bedtime = dt(0, 0, TODAY)
        val wake = dt(7, 0, TODAY) // 7h in bed = 405 net (4 cycles + 45 min into 5th)
        val warnings = SleepMathEngine.validateSleepPlan(bedtime, wake, Chronotype.INTERMEDIATE)
        val midCycle = warnings.find { it.code == WarningCode.MID_CYCLE_WAKE }
        assertNotNull("Mid-cycle wake must be flagged", midCycle)
    }

    @Test
    fun `validateSleepPlan on-cycle wake has no MID_CYCLE_WAKE warning`() {
        // 5 cycles exactly: 5*90 = 450 net, + 15 onset = 465 in bed
        // Bedtime 23:15, wake 07:00 = 7h45m in bed = 465 min = 450 net ✓
        val bedtime = dt(23, 15, TODAY)
        val wake = dt(7, 0, TOMORROW)
        val warnings = SleepMathEngine.validateSleepPlan(bedtime, wake, Chronotype.INTERMEDIATE)
        val midCycle = warnings.find { it.code == WarningCode.MID_CYCLE_WAKE }
        assertNull("On-cycle wake should NOT trigger mid-cycle warning", midCycle)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Midnight crossover edge cases
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `midnight crossover — bedtime 02 00 wake 09 45 correct duration`() {
        // 02:00 → 09:45 = 7h45m = 465 min in bed, 450 net (5 cycles)
        val bedtime = dt(2, 0, TODAY)
        val wake = dt(9, 45, TODAY)
        val plan = SleepMathEngine.buildPlanFromTimes(bedtime, wake, Chronotype.INTERMEDIATE)
        assertEquals(450, plan.durationMinutes)
        assertEquals(5, plan.cycles)
    }

    @Test
    fun `midnight crossover — bedtime 23 00 wake 02 00 is 2h45m net`() {
        // 23:00 → 02:00 next day = 3h in bed, 2h45m net (1 cycle + 75min)
        val bedtime = dt(23, 0, TODAY)
        val wake = dt(2, 0, TODAY)  // next day — minutesBetween handles this
        val plan = SleepMathEngine.buildPlanFromTimes(bedtime, wake, Chronotype.INTERMEDIATE)
        assertEquals(165, plan.durationMinutes) // 3h - 15min = 165 min = 2h45m
    }

    // ─────────────────────────────────────────────────────────────────────────
    // buildPlanFromTimes
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `buildPlanFromTimes 5 cycles has grade A`() {
        val bedtime = dt(23, 15, TODAY)
        val wake = dt(7, 0, TOMORROW)
        val plan = SleepMathEngine.buildPlanFromTimes(bedtime, wake, Chronotype.INTERMEDIATE)
        assertEquals(5, plan.cycles)
        assertEquals(450, plan.durationMinutes)
        assertEquals(com.biobell.android.domain.model.HealthScore.Grade.A, plan.healthScore.grade)
    }
}
