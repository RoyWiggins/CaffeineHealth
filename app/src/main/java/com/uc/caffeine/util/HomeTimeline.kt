package com.uc.caffeine.util

import com.uc.caffeine.data.UserSettings
import com.uc.caffeine.data.model.ConsumptionEntry
import com.uc.caffeine.data.model.HeadacheEntry
import java.time.Instant
import java.time.LocalDate
import java.util.LinkedHashMap

/**
 * A single row in the Home screen timeline. Drinks and reported headaches are
 * merged into one chronological stream so a headache shows up right next to the
 * caffeine that was (or wasn't) on board at the time.
 */
sealed interface HomeTimelineItem {
    val timestampMillis: Long

    data class Drink(val entry: ConsumptionEntry) : HomeTimelineItem {
        // Group/sort by when the caffeine takes effect, so delayed-release doses
        // shift forward into the day they actually kick in.
        override val timestampMillis: Long get() = entry.effectiveStartMillis
    }

    /**
     * A reported headache plus the active caffeine level inferred from the
     * consumption log at the moment it occurred.
     */
    data class Headache(
        val entry: HeadacheEntry,
        val inferredCaffeineMg: Double,
    ) : HomeTimelineItem {
        override val timestampMillis: Long get() = entry.startedAtMillis
    }
}

/**
 * Build the merged, date-grouped Home timeline. Each headache's inferred caffeine
 * level is computed from [entries] using the user's effective half-life.
 */
fun buildHomeTimeline(
    entries: List<ConsumptionEntry>,
    headaches: List<HeadacheEntry>,
    settings: UserSettings,
): Map<LocalDate, List<HomeTimelineItem>> {
    val zoneId = settings.resolvedZoneId()
    val halfLife = settings.effectiveHalfLifeMinutes

    val items = buildList<HomeTimelineItem> {
        entries.forEach { add(HomeTimelineItem.Drink(it)) }
        headaches.forEach { headache ->
            val inferred = CaffeineCalculator.calculateCurrentLevel(
                entries = entries,
                currentTimeMillis = headache.startedAtMillis,
                halfLifeMinutes = halfLife,
            )
            add(HomeTimelineItem.Headache(headache, inferred))
        }
    }

    return items
        .sortedByDescending { it.timestampMillis }
        .groupBy { item ->
            Instant.ofEpochMilli(item.timestampMillis).atZone(zoneId).toLocalDate()
        }
        .toList()
        .sortedByDescending { (date, _) -> date }
        .toMap(LinkedHashMap())
}
