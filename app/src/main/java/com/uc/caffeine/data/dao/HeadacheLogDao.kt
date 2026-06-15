package com.uc.caffeine.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.uc.caffeine.data.model.HeadacheEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface HeadacheLogDao {

    @Insert
    suspend fun insert(entry: HeadacheEntry): Long

    @Query("DELETE FROM headache_log WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM headache_log ORDER BY startedAtMillis DESC")
    fun getAll(): Flow<List<HeadacheEntry>>

    @Query("SELECT * FROM headache_log ORDER BY startedAtMillis DESC")
    suspend fun getAllOnce(): List<HeadacheEntry>

    @Query("DELETE FROM headache_log")
    suspend fun deleteAll()
}
