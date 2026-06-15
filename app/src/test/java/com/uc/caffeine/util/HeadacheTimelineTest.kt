package com.uc.caffeine.util

import com.uc.caffeine.data.UserSettings
import com.uc.caffeine.data.model.ConsumptionEntry
import com.uc.caffeine.data.model.HeadacheEntry
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HeadacheTimelineTest {

    private val settings = UserSettings(timeZoneId = "UTC")

    private fun millis(iso: String) = Instant.parse(iso).toEpochMilli()

    @Test
    fun buildHomeTimeline_infersCaffeineLevelAtHeadacheTime() {
        val drink = ConsumptionEntry(
            id = 1,
            drinkName = "Coffee",
            caffeineMg = 100,
            emoji = "☕",
            startedAtMillis = millis("2026-04-05T08:00:00Z"),
        )
        val headacheAfter = HeadacheEntry(id = 1, startedAtMillis = millis("2026-04-05T11:00:00Z"))
        val headacheBefore = HeadacheEntry(id = 2, startedAtMillis = millis("2026-04-05T06:00:00Z"))

        val timeline = buildHomeTimeline(
            entries = listOf(drink),
            headaches = listOf(headacheAfter, headacheBefore),
            settings = settings,
        )

        val items = timeline.values.flatten().filterIsInstance<HomeTimelineItem.Headache>()
        val after = items.first { it.entry.id == 1 }
        val before = items.first { it.entry.id == 2 }

        // Caffeine on board 3h after a 100 mg coffee, but none an hour before drinking it.
        assertTrue("expected caffeine on board after drinking", after.inferredCaffeineMg > 0.0)
        assertEquals("expected no caffeine before drinking", 0.0, before.inferredCaffeineMg, 1e-9)
    }

    @Test
    fun buildHomeTimeline_mergesDrinksAndHeadachesInReverseChronologicalOrder() {
        val drink = ConsumptionEntry(
            id = 1,
            drinkName = "Coffee",
            caffeineMg = 100,
            emoji = "☕",
            startedAtMillis = millis("2026-04-05T08:00:00Z"),
        )
        val headache = HeadacheEntry(id = 1, startedAtMillis = millis("2026-04-05T11:00:00Z"))

        val dayItems = buildHomeTimeline(listOf(drink), listOf(headache), settings)
            .values.flatten()

        // Most recent first: the 11:00 headache precedes the 08:00 drink.
        assertTrue(dayItems.first() is HomeTimelineItem.Headache)
        assertTrue(dayItems[1] is HomeTimelineItem.Drink)
    }

    @Test
    fun generateChartData_emitsHeadacheMarkersWithInferredLevel() {
        val now = millis("2026-04-05T12:00:00Z")
        val drink = ConsumptionEntry(
            id = 1,
            drinkName = "Coffee",
            caffeineMg = 100,
            emoji = "☕",
            startedAtMillis = millis("2026-04-05T08:00:00Z"),
        )
        val headache = HeadacheEntry(id = 7, startedAtMillis = millis("2026-04-05T10:00:00Z"))

        val data = ChartDataGenerator.generateChartData(
            entries = listOf(drink),
            settings = settings,
            currentTime = now,
            headaches = listOf(headache),
        )

        assertEquals(1, data.headacheMarkers.size)
        val marker = data.headacheMarkers.first()
        assertEquals(7, marker.headacheId)
        assertTrue(marker.inferredCaffeineMg > 0.0)
    }
}
