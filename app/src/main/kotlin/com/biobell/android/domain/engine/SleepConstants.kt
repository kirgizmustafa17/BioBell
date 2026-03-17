package com.biobell.android.domain.engine

/**
 * Sleep science constants used throughout the SleepMathEngine.
 *
 * These are the core numbers that drive BioBell's biology-aware scheduling.
 */
object SleepConstants {
    /** Minutes it takes the average person to fall asleep after lying down */
    const val SLEEP_ONSET_MINUTES = 15

    /** Duration of one full sleep cycle in minutes */
    const val CYCLE_DURATION_MINUTES = 90

    /** Minimum number of cycles for a "healthy" sleep (6 hours) */
    const val MIN_HEALTHY_CYCLES = 4

    /** Minimum healthy sleep duration in minutes (4 cycles = 6h) */
    const val MIN_HEALTHY_DURATION_MINUTES = MIN_HEALTHY_CYCLES * CYCLE_DURATION_MINUTES // 360

    /** Recommended sleep duration in minutes (5 cycles = 7h30m) */
    const val RECOMMENDED_DURATION_MINUTES = 5 * CYCLE_DURATION_MINUTES // 450

    /** Minimum cycles to suggest (users below 4 cycles get a warning) */
    const val MIN_SUGGESTION_CYCLES = 3  // 4h30m — still shown but flagged as error

    /** Maximum cycles to suggest (more than 6 is over-sleeping) */
    const val MAX_SUGGESTION_CYCLES = 6  // 9h00m
}
