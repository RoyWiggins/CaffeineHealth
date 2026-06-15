package com.uc.caffeine.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A user-reported headache. We store only the time it occurred (plus optional
 * severity and note); the caffeine level "at that time" is always inferred from
 * the consumption log, so it stays correct even if drinks are edited later.
 */
@Entity(tableName = "headache_log")
data class HeadacheEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // When the headache occurred — the canonical timestamp for sorting and display.
    val startedAtMillis: Long = System.currentTimeMillis(),

    // 1 = mild, 2 = moderate, 3 = severe.
    val severity: Int = HeadacheSeverity.MODERATE.level,

    // Optional free-text note.
    val note: String = "",
)

/**
 * Severity buckets for a reported headache. Stored as [level] on [HeadacheEntry].
 */
enum class HeadacheSeverity(val level: Int) {
    MILD(1),
    MODERATE(2),
    SEVERE(3),
    ;

    companion object {
        fun fromLevel(level: Int): HeadacheSeverity =
            entries.find { it.level == level } ?: MODERATE
    }
}
