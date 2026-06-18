package com.uc.caffeine.util

import com.uc.caffeine.data.UserSettings
import com.uc.caffeine.data.model.ConsumptionEntry
import java.time.Instant
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChartConsumptionMarkerOverdueTest {

    private val settings = UserSettings(timeZoneId = "UTC")
    private fun millis(iso: String) = Instant.parse(iso).toEpochMilli()
    private val now = millis("2026-04-08T12:00:00Z")

    private fun overdueFor(entry: ConsumptionEntry): Boolean {
        val data = ChartDataGenerator.generateChartData(
            entries = listOf(entry),
            settings = settings,
            currentTime = now,
        )
        return data.consumptionMarkers
            .flatMap { it.entries }
            .first { it.entryId == entry.id }
            .overdue
    }

    private fun entry(id: Int, startedAt: String, taken: Boolean) = ConsumptionEntry(
        id = id,
        drinkName = "Coffee",
        caffeineMg = 100,
        emoji = "☕",
        startedAtMillis = millis(startedAt),
        taken = taken,
    )

    @Test
    fun pastNotTaken_isOverdue() {
        assertTrue(overdueFor(entry(1, "2026-04-08T10:00:00Z", taken = false)))
    }

    @Test
    fun pastTaken_isNotOverdue() {
        assertFalse(overdueFor(entry(2, "2026-04-08T10:00:00Z", taken = true)))
    }

    @Test
    fun futureNotTaken_isNotOverdue() {
        assertFalse(overdueFor(entry(3, "2026-04-08T14:00:00Z", taken = false)))
    }
}
