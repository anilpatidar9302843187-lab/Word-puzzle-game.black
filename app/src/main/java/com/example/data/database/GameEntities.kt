package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "level_progress")
data class LevelProgress(
    @PrimaryKey val levelId: Int,
    val completed: Boolean = false,
    val foundWords: String = "", // Comma-separated list of found words (e.g. "ARE,EAR")
    val scoreEarned: Int = 0
)

@Entity(tableName = "game_stats")
data class GameStats(
    @PrimaryKey val id: Int = 1, // Singleton stats row
    val currentLevelId: Int = 1,
    val totalScore: Int = 0,
    val highScore: Int = 0,
    val hintsAvailable: Int = 3,
    val soundEnabled: Boolean = true
)
