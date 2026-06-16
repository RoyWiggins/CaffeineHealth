package com.uc.caffeine.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.uc.caffeine.data.dao.ConsumptionLogDao
import com.uc.caffeine.data.dao.DrinkPresetDao
import com.uc.caffeine.data.dao.DrinkUnitDao
import com.uc.caffeine.data.dao.HeadacheLogDao
import com.uc.caffeine.data.model.ConsumptionEntry
import com.uc.caffeine.data.model.DrinkPreset
import com.uc.caffeine.data.model.DrinkUnit
import com.uc.caffeine.data.model.HeadacheEntry
import com.uc.caffeine.data.model.defaultDrinkPresets
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [DrinkPreset::class, DrinkUnit::class, ConsumptionEntry::class, HeadacheEntry::class],
    version = 13,
    exportSchema = false
)
abstract class CaffeineDatabase : RoomDatabase() {

    abstract fun drinkPresetDao(): DrinkPresetDao
    abstract fun drinkUnitDao(): DrinkUnitDao
    abstract fun consumptionLogDao(): ConsumptionLogDao
    abstract fun headacheLogDao(): HeadacheLogDao

    companion object {
        @Volatile
        private var INSTANCE: CaffeineDatabase? = null

        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE consumption_log ADD COLUMN healthConnectRecordId TEXT")
            }
        }

        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE drink_presets ADD COLUMN delayMinutes INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE consumption_log ADD COLUMN delayMinutes INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS headache_log (
                        id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        startedAtMillis INTEGER NOT NULL,
                        severity INTEGER NOT NULL DEFAULT 2,
                        note TEXT NOT NULL DEFAULT ''
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE drink_presets ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): CaffeineDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CaffeineDatabase::class.java,
                    "caffeine_database"
                )
                    .addMigrations(MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13)
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Seed in background when DB is first created
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.let { database ->
                                    seedDatabase(context, database)
                                }
                            }
                        }
                    })
                    .build()

                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedDatabase(context: Context, db: CaffeineDatabase) {
            // Try JSON first — 220 items with full data
            val jsonItems = DrinkJsonImporter.importFromAssets(context)

            if (jsonItems.isNotEmpty()) {
                for (result in jsonItems) {
                    // Insert preset and get its auto-generated ID back
                    val presetId = db.drinkPresetDao().insertAndGetId(result.preset).toInt()

                    // Now insert all its units with the real drinkId
                    val unitsWithId = result.units.map { it.copy(drinkId = presetId) }
                    db.drinkUnitDao().insertAll(unitsWithId)
                }
            } else {
                // Fallback: hardcoded minimal presets if JSON is missing
                defaultDrinkPresets.forEach { preset ->
                    val id = db.drinkPresetDao().insertAndGetId(preset).toInt()
                    // Create one default unit per preset
                    db.drinkUnitDao().insert(
                        DrinkUnit(
                            drinkId     = id,
                            unitKey     = preset.defaultUnit,
                            caffeineMg  = 80.0,
                            milliliters = 240.0,
                            grams       = null,
                            isDefault   = true
                        )
                    )
                }
            }
        }
    }
}