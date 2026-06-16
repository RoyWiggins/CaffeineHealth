package com.uc.caffeine.util

import com.uc.caffeine.data.UserSettings
import com.uc.caffeine.data.model.ConsumptionEntry
import java.time.Instant
import java.time.LocalDate

/**
 * One day's caffeine concentration sampled evenly across 24 hours, for the
 * radial (polar) Home view. [samples] runs from midnight (index 0) through the
 * day; index i corresponds to time-of-day fraction i / samples.size.
 */
data class RadialDayTrace(
    val dayOffset: Int,   // 0 = today, increasing = further in the past
    val date: LocalDate,
    val samples: List<Double>,
)

data class RadialCaffeineData(
    val traces: List<RadialDayTrace>,
    val maxConcentrationMg: Double,
    val samplesPerDay: Int,
) {
    companion object {
        val EMPTY = RadialCaffeineData(emptyList(), 0.0, 0)
    }
}

/**
 * Sample the active caffeine concentration across each of the last seven days so
 * they can be drawn wrapped around a single 24-hour clock.
 */
fun buildRadialCaffeineData(
    entries: List<ConsumptionEntry>,
    settings: UserSettings,
    nowMillis: Long = System.currentTimeMillis(),
    samplesPerDay: Int = 48,
): RadialCaffeineData {
    if (samplesPerDay <= 0) return RadialCaffeineData.EMPTY
    val zoneId = settings.resolvedZoneId()
    val halfLife = settings.effectiveHalfLifeMinutes
    val today = Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalDate()

    val traces = (0..6).map { offset ->
        val date = today.minusDays(offset.toLong())
        val dayStart = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val dayEnd = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val stepMillis = (dayEnd - dayStart) / samplesPerDay
        // Caffeine older than ~3 days is negligible — bound the work per sample.
        val windowStart = date.minusDays(3).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val relevant = entries.filter { it.effectiveStartMillis in windowStart until dayEnd }
        val samples = (0 until samplesPerDay).map { i ->
            CaffeineCalculator.calculateCurrentLevel(
                entries = relevant,
                currentTimeMillis = dayStart + i * stepMillis,
                halfLifeMinutes = halfLife,
            )
        }
        RadialDayTrace(dayOffset = offset, date = date, samples = samples)
    }
    val maxConcentration = traces.flatMap(RadialDayTrace::samples).maxOrNull() ?: 0.0
    return RadialCaffeineData(
        traces = traces,
        maxConcentrationMg = maxConcentration,
        samplesPerDay = samplesPerDay,
    )
}
