package com.biobell.android.domain.model

/**
 * A health score (0–100) describing the quality of a sleep plan.
 *
 * Grade scale:
 *  A  90–100  Excellent (≥7h30m, on cycle, chronotype-aligned)
 *  B  75–89   Good (≥7h, minor issues)
 *  C  60–74   Fair (≥6h, off-cycle or mild chronotype mismatch)
 *  D  40–59   Poor (5–6h or significant issues)
 *  F  0–39    Failing (< 5h or severe issues)
 */
data class HealthScore(
    val score: Int,          // 0–100
    val grade: Grade,
    val summary: String,     // Short human description e.g. "Great — 5 full cycles"
) {
    enum class Grade(val label: String) {
        A("A"), B("B"), C("C"), D("D"), F("F")
    }

    companion object {
        fun fromScore(score: Int, summary: String): HealthScore {
            val grade = when {
                score >= 90 -> Grade.A
                score >= 75 -> Grade.B
                score >= 60 -> Grade.C
                score >= 40 -> Grade.D
                else        -> Grade.F
            }
            return HealthScore(score.coerceIn(0, 100), grade, summary)
        }
    }
}
