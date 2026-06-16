package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM level_progress")
    fun getAllLevelProgress(): Flow<List<LevelProgress>>

    @Query("SELECT * FROM level_progress WHERE levelId = :levelId LIMIT 1")
    fun getLevelProgressById(levelId: Int): Flow<LevelProgress?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLevelProgress(levelProgress: LevelProgress)

    @Query("SELECT * FROM game_stats WHERE id = 1 LIMIT 1")
    fun getGameStats(): Flow<GameStats?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGameStats(gameStats: GameStats)

    @Query("DELETE FROM level_progress")
    suspend fun clearAllLevelProgress()
}
