package com.uc.caffeine.util

import com.uc.caffeine.data.UserSettings
import com.uc.caffeine.data.model.ConsumptionEntry
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RadialCaffeineDataTest {

    private val settings = UserSettings(timeZoneId = "UTC")

    private fun millis(iso: String) = Instant.parse(iso).toEpochMilli()

    @Test
    fun buildRadialCaffeineData_producesSevenEvenlySampledTraces() {
        val now = millis("2026-04-08T12:00:00Z")
        val data = buildRadialCaffeineData(
            entries = emptyList(),
            settings = settings,
            nowMillis = now,
            samplesPerDay = 48,
        )

        assertEquals(7, data.traces.size)
        assertEquals(listOf(0, 1, 2, 3, 4, 5, 6), data.traces.map { it.dayOffset })
        data.traces.forEach { assertEquals(48, it.samples.size) }
        // No entries → flat zero everywhere.
        assertEquals(0.0, data.maxConcentrationMg, 1e-9)
    }

    @Test
    fun buildRadialCaffeineData_reflectsCaffeineOnTheDayConsumed() {
        val now = millis("2026-04-08T23:00:00Z")
        val coffee = ConsumptionEntry(
            id = 1,
            drinkName = "Coffee",
            caffeineMg = 100,
            emoji = "☕",
            startedAtMillis = millis("2026-04-08T08:00:00Z"),
        )
        val data = buildRadialCaffeineData(
            entries = listOf(coffee),
            settings = settings,
            nowMillis = now,
            samplesPerDay = 48,
        )

        assertTrue(data.maxConcentrationMg > 0.0)
        val today = data.traces.first { it.dayOffset == 0 }
        // Sample index 16 ≈ 08:00 (16/48 of the day) — caffeine should be present.
        assertTrue(today.samples[17] > 0.0)
        // A week ago there was nothing.
        val weekAgo = data.traces.first { it.dayOffset == 6 }
        assertEquals(0.0, weekAgo.samples.max(), 1e-9)
    }
}
