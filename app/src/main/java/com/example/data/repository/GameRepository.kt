package com.example.data.repository

import com.example.data.database.GameDao
import com.example.data.database.LevelProgress
import com.example.data.database.GameStats
import kotlinx.coroutines.flow.Flow

class GameRepository(private val gameDao: GameDao) {
    val allLevelProgress: Flow<List<LevelProgress>> = gameDao.getAllLevelProgress()
    val gameStats: Flow<GameStats?> = gameDao.getGameStats()

    fun getLevelProgress(levelId: Int): Flow<LevelProgress?> = gameDao.getLevelProgressById(levelId)

    suspend fun saveLevelProgress(progress: LevelProgress) {
        gameDao.saveLevelProgress(progress)
    }

    suspend fun saveGameStats(stats: GameStats) {
        gameDao.saveGameStats(stats)
    }

    suspend fun resetGame() {
        gameDao.clearAllLevelProgress()
        gameDao.saveGameStats(GameStats())
    }
}
