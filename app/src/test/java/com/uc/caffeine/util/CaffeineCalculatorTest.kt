package com.uc.caffeine.util

import com.uc.caffeine.data.model.ConsumptionEntry
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CaffeineCalculatorTest {

    @Test
    fun calculatePeakTime_longerDurationProducesLaterFlatterPeak() {
        val quickDrink = testEntry(
            id = 1,
            startedAt = "2026-04-05T12:00:00Z",
            durationMinutes = 1,
        )
        val slowDrink = testEntry(
            id = 2,
            startedAt = "2026-04-05T12:00:00Z",
            durationMinutes = 10,
        )

        val quickPeakTime = CaffeineCalculator.calculatePeakTime(quickDrink, halfLifeMinutes = 300)
        val slowPeakTime = CaffeineCalculator.calculatePeakTime(slowDrink, halfLifeMinutes = 300)
        val quickPeakLevel = CaffeineCalculator.calculateEntryContribution(quickDrink, quickPeakTime, 300)
        val slowPeakLevel = CaffeineCalculator.calculateEntryContribution(slowDrink, slowPeakTime, 300)

        assertTrue(slowPeakTime > quickPeakTime)
        assertTrue(slowPeakLevel < quickPeakLevel)
    }

    @Test
    fun calculatePeakTime_preservesAbsorptionRateDifferences() {
        val fastAbsorption = testEntry(
            id = 1,
            startedAt = "2026-04-05T12:00:00Z",
            absorptionRate = 20,
        )
        val slowAbsorption = testEntry(
            id = 2,
            startedAt = "2026-04-05T12:00:00Z",
            absorptionRate = 60,
        )

        val fastPeakTime = CaffeineCalculator.calculatePeakTime(fastAbsorption, halfLifeMinutes = 300)
        val slowPeakTime = CaffeineCalculator.calculatePeakTime(slowAbsorption, halfLifeMinutes = 300)

        assertTrue(slowPeakTime > fastPeakTime)
    }

    @Test
    fun predictTimeUntilLevel_returnsFutureTimestampForDistributedEntry() {
        val entry = testEntry(
            id = 1,
            startedAt = "2026-04-05T12:00:00Z",
            caffeineMg = 220,
            durationMinutes = 30,
        )
        val now = Instant.parse("2026-04-05T12:20:00Z").toEpochMilli()

        val predicted = CaffeineCalculator.predictTimeUntilLevel(
            entries = listOf(entry),
            targetLevelMg = 5.0,
            currentTimeMillis = now,
            halfLifeMinutes = 300,
        )

        assertNotNull(predicted)
        assertTrue(predicted!! > now)
    }

    @Test
    fun delayedRelease_holdsCaffeineUntilDelayElapses() {
        val started = "2026-04-05T12:00:00Z"
        val instant = testEntry(id = 1, startedAt = started)
        val delayed = testEntry(id = 2, startedAt = started, delayMinutes = 180)

        // 30 minutes after consumption: the instant drink is already absorbing,
        // the delayed-release pill has not started yet.
        val thirtyMinutesIn = Instant.parse("2026-04-05T12:30:00Z").toEpochMilli()
        val instantLevel = CaffeineCalculator.calculateEntryContribution(instant, thirtyMinutesIn, 300)
        val delayedLevel = CaffeineCalculator.calculateEntryContribution(delayed, thirtyMinutesIn, 300)

        assertTrue(instantLevel > 0.0)
        assertEquals(0.0, delayedLevel, 1e-9)
    }

    @Test
    fun delayedRelease_shiftsPeakLaterByTheDelay() {
        val started = "2026-04-05T12:00:00Z"
        val instant = testEntry(id = 1, startedAt = started)
        val delayed = testEntry(id = 2, startedAt = started, delayMinutes = 180)

        val instantPeak = CaffeineCalculator.calculatePeakTime(instant, halfLifeMinutes = 300)
        val delayedPeak = CaffeineCalculator.calculatePeakTime(delayed, halfLifeMinutes = 300)

        val shiftMinutes = (delayedPeak - instantPeak) / 60_000L
        // The peak should move later by roughly the delay (allowing minor search granularity).
        assertTrue(shiftMinutes in 175..185)
    }

    private fun testEntry(
        id: Int,
        startedAt: String,
        caffeineMg: Int = 100,
        durationMinutes: Int = 10,
        absorptionRate: Int = 45,
        delayMinutes: Int = 0,
    ): ConsumptionEntry {
        return ConsumptionEntry(
            id = id,
            drinkName = "Drink $id",
            caffeineMg = caffeineMg,
            emoji = "\u2615",
            absorptionRate = absorptionRate,
            delayMinutes = delayMinutes,
            startedAtMillis = Instant.parse(startedAt).toEpochMilli(),
            durationMinutes = durationMinutes,
        )
    }
}
