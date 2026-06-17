package com.uc.caffeine.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

const val DEFAULT_CONSUMPTION_DURATION_MINUTES = 10
private const val MINUTE_IN_MILLIS = 60_000L

// This table stores EVERY drink the user logs — one row per drink consumed
// This is how we get:
//   - Today's total  (sum WHERE timestamp is today)
//   - History        (group by day)
//   - Half-life math (we know exact time each drink was consumed)
@Entity(tableName = "consumption_log")
data class ConsumptionEntry(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // We store the drink name + mg directly here (not a foreign key)
    // Why? Because if the user later deletes a preset, history should still show correctly
    val drinkName: String,
    val caffeineMg: Int,
    val emoji: String,

    // Stable preset identifier so we can recover units when editing later.
    val presetItemId: String = "",

    // The serving selection used for this logged entry.
    val quantity: Int = 1,
    val unitKey: String = "",
    val unitCaffeineMg: Double = 0.0,

    // for images of items
    val imageName: String = "",

    // Absorption rate in minutes - time to peak blood concentration
    // Stored here (not FK) so historical data remains accurate even if preset changes
    val absorptionRate: Int = 45,

    // Release delay in minutes — how long after consumption the caffeine actually
    // starts entering the bloodstream. 0 for ordinary drinks; > 0 for delayed-release
    // pills that only kick in some hours later. Stored here (not FK) so historical
    // data stays accurate even if the preset changes.
    val delayMinutes: Int = 0,

    // Start time for this consumption window.
    // This is the canonical timestamp used for sorting, grouping, and display.
    val startedAtMillis: Long = System.currentTimeMillis(),

    // The number of whole minutes the user takes to finish the drink.
    val durationMinutes: Int = DEFAULT_CONSUMPTION_DURATION_MINUTES,

    // Non-null = this row was imported from another app via Health Connect (we don't own it).
    // Null = logged by this app (we own it and push it to HC).
    val healthConnectRecordId: String? = null,

    // Whether the drink has actually been taken. Entries logged in the past start
    // taken; entries scheduled for the future start not-taken and can be confirmed
    // later (in-app or from the reminder notification).
    val taken: Boolean = true,
) {
    val normalizedDurationMinutes: Int
        get() = durationMinutes.coerceAtLeast(1)

    val finishedAtMillis: Long
        get() = startedAtMillis + (normalizedDurationMinutes * MINUTE_IN_MILLIS)

    /**
     * When the caffeine actually begins entering the bloodstream, accounting for any
     * delayed-release [delayMinutes]. Equal to [startedAtMillis] for ordinary drinks.
     */
    val effectiveStartMillis: Long
        get() = startedAtMillis + (delayMinutes.coerceAtLeast(0) * MINUTE_IN_MILLIS)

    /**
     * When the (delayed) consumption window finishes — used for peak-time search bounds.
     */
    val effectiveFinishMillis: Long
        get() = effectiveStartMillis + (normalizedDurationMinutes * MINUTE_IN_MILLIS)
}
