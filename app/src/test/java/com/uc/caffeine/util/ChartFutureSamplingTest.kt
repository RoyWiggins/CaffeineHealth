package com.uc.caffeine.util

import com.uc.caffeine.data.UserSettings
import com.uc.caffeine.data.model.ConsumptionEntry
import java.time.Instant
import org.junit.Assert.assertTrue
import org.junit.Test

class ChartFutureSamplingTest {

    private val settings = UserSettings(timeZoneId = "UTC")

    private fun millis(iso: String) = Instant.parse(iso).toEpochMilli()

    @Test
    fun futurePointsWithinThreeDaysAreFifteenMinutesApart() {
        val now = millis("2026-04-08T12:00:00Z")
        // A large recent dose keeps the curve (and its sampled points) running
        // well into the future.
        val entry = ConsumptionEntry(
            id = 1,
            drinkName = "Big coffee",
            caffeineMg = 600,
            emoji = "☕",
            startedAtMillis = now,
        )

        val data = ChartDataGenerator.generateChartData(
            entries = listOf(entry),
            settings = settings,
            currentTime = now,
        )

        val threeDays = 3L * 24 * 60 * 60 * 1000
        val fifteenMin = 15L * 60 * 1000
        val futurePoints = data.dataPoints
            .filter { it.timestampMillis > now && it.timestampMillis <= now + threeDays }
            .sortedBy { it.timestampMillis }

        assertTrue("expected plenty of future points", futurePoints.size > 48)
        val gaps = futurePoints.zipWithNext { a, b -> b.timestampMillis - a.timestampMillis }
        assertTrue("future points within 3 days should be <= 15 min apart", gaps.all { it <= fifteenMin })
    }
}
