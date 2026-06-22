package com.uc.caffeine.util

import com.uc.caffeine.data.UserSettings
import com.uc.caffeine.data.model.ConsumptionEntry
import com.uc.caffeine.data.model.HeadacheEntry
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AnalyticsWithdrawalStatsTest {

    private val settings = UserSettings(
        timeZoneId = "UTC",
        withdrawalThresholdEnabled = true,
        withdrawalThresholdMg = 30,
    )

    private fun millis(iso: String) = Instant.parse(iso).toEpochMilli()

    @Test
    fun last7DaysIntake_countsDelayedPillOnTheDayItKicksIn() {
        // Taken at 23:30 on the 5th, but with a 60-minute release delay it only
        // begins entering the bloodstream at 00:30 on the 6th.
        val delayedPill = ConsumptionEntry(
            id = 1,
            drinkName = "Slow caffeine pill",
            caffeineMg = 200,
            emoji = "💊",
            startedAtMillis = millis("2026-04-05T23:30:00Z"),
            delayMinutes = 60,
        )
        val now = millis("2026-04-06T12:00:00Z")

        val state = buildAnalyticsUiState(
            entries = listOf(delayedPill),
            presets = emptyList(),
            settings = settings,
            selectedRange = AnalyticsRange.LAST_30_DAYS,
            nowMillis = now,
            headaches = emptyList(),
        )

        val byDate = state.last7DaysIntake.associate { it.date to it.totalMg }
        assertEquals(200, byDate[LocalDate.parse("2026-04-06")])
        assertEquals(0, byDate[LocalDate.parse("2026-04-05")])
        assertEquals(7, state.last7DaysIntake.size)
    }

    @Test
    fun withdrawalStats_flagBelowThresholdAndHeadacheDays() {
        val drink = ConsumptionEntry(
            id = 1,
            drinkName = "Coffee",
            caffeineMg = 100,
            emoji = "☕",
            startedAtMillis = millis("2026-04-05T08:00:00Z"),
        )
        val headache = HeadacheEntry(id = 1, startedAtMillis = millis("2026-04-05T07:00:00Z"))
        val now = millis("2026-04-06T12:00:00Z")

        val state = buildAnalyticsUiState(
            entries = listOf(drink),
            presets = emptyList(),
            settings = settings,
            selectedRange = AnalyticsRange.LAST_30_DAYS,
            nowMillis = now,
            headaches = listOf(headache),
        )

        val stat = state.withdrawalDailyStats[LocalDate.parse("2026-04-05")]
        assertTrue(stat != null)
        // Before the 8am coffee there was no caffeine on board, so the day's
        // minimum is below the 30 mg threshold.
        assertTrue(stat!!.belowThreshold)
        assertTrue(stat.hadHeadache)
    }

    @Test
    fun withdrawalStats_disabledThresholdNeverFlagsBelow() {
        val disabled = settings.copy(withdrawalThresholdEnabled = false)
        val drink = ConsumptionEntry(
            id = 1,
            drinkName = "Coffee",
            caffeineMg = 100,
            emoji = "☕",
            startedAtMillis = millis("2026-04-05T08:00:00Z"),
        )
        val now = millis("2026-04-06T12:00:00Z")

        val state = buildAnalyticsUiState(
            entries = listOf(drink),
            presets = emptyList(),
            settings = disabled,
            selectedRange = AnalyticsRange.LAST_30_DAYS,
            nowMillis = now,
            headaches = emptyList(),
        )

        val stat = state.withdrawalDailyStats[LocalDate.parse("2026-04-05")]
        assertTrue(stat != null)
        assertFalse(stat!!.belowThreshold)
    }
}
