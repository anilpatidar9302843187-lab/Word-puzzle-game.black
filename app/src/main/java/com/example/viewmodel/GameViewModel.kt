package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.GameStats
import com.example.data.database.LevelProgress
import com.example.data.model.GameLevel
import com.example.data.model.LevelRegistry
import com.example.data.repository.GameRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Collections

class GameViewModel(private val repository: GameRepository) : ViewModel() {

    // --- Core Reactive Flow States ---
    private val _gameStats = MutableStateFlow(GameStats())
    val gameStatsState: StateFlow<GameStats> = _gameStats.asStateFlow()

    private val _currentLevel = MutableStateFlow(LevelRegistry.levels.first())
    val currentLevelState: StateFlow<GameLevel> = _currentLevel.asStateFlow()

    private val _scrambledLetters = MutableStateFlow<List<Char>>(emptyList())
    val scrambledLettersState: StateFlow<List<Char>> = _scrambledLetters.asStateFlow()

    private val _activeInput = MutableStateFlow("")
    val activeInputState: StateFlow<String> = _activeInput.asStateFlow()

    private val _clickedLetterIndices = MutableStateFlow<List<Int>>(emptyList())
    val clickedLetterIndicesState: StateFlow<List<Int>> = _clickedLetterIndices.asStateFlow()

    private val _foundWords = MutableStateFlow<Set<String>>(emptySet())
    val foundWordsState: StateFlow<Set<String>> = _foundWords.asStateFlow()

    // --- Tactile Animations and Popup UI state ---
    private val _isCorrectSplash = MutableStateFlow(false)
    val isCorrectSplashState: StateFlow<Boolean> = _isCorrectSplash.asStateFlow()

    private val _isErrorShake = MutableStateFlow(false)
    val isErrorShakeState: StateFlow<Boolean> = _isErrorShake.asStateFlow()

    private val _activeFeedbackMessage = MutableStateFlow<String?>(null)
    val activeFeedbackMessageState: StateFlow<String?> = _activeFeedbackMessage.asStateFlow()

    private val _selectedWordClue = MutableStateFlow<Pair<String, String>?>(null) // Word to Clue mapping
    val selectedWordClueState: StateFlow<Pair<String, String>?> = _selectedWordClue.asStateFlow()

    private val _levelCompletedSuccessfully = MutableStateFlow(false)
    val levelCompletedSuccessfullyState: StateFlow<Boolean> = _levelCompletedSuccessfully.asStateFlow()

    init {
        // Observe database stats and active level settings
        viewModelScope.launch {
            repository.gameStats
                .filterNotNull()
                .collect { stats ->
                    _gameStats.value = stats
                    // Switch current level configuration if it changed
                    if (_currentLevel.value.id != stats.currentLevelId) {
                        val level = LevelRegistry.getById(stats.currentLevelId)
                        _currentLevel.value = level
                        resetInputAndScramble(level)
                    }
                }
        }

        // Initialize Level Progress observation
        viewModelScope.launch {
            combine(_currentLevel, repository.allLevelProgress) { level, allProgress ->
                val progress = allProgress.firstOrNull { it.levelId == level.id }
                progress to level
            }.collect { (progress, level) ->
                if (progress != null) {
                    val savedFound = progress.foundWords
                        .split(",")
                        .filter { it.isNotBlank() }
                        .toSet()
                    _foundWords.value = savedFound
                } else {
                    _foundWords.value = emptySet()
                }
            }
        }

        // Scramble letters initially
        resetInputAndScramble(_currentLevel.value)
    }

    private fun resetInputAndScramble(level: GameLevel) {
        _activeInput.value = ""
        _clickedLetterIndices.value = emptyList()
        val shuffled = level.letters.shuffled()
        _scrambledLetters.value = shuffled
        _levelCompletedSuccessfully.value = false
        _activeFeedbackMessage.value = "Spell the hidden words!"
    }

    fun selectLetter(index: Int) {
        val scrambled = _scrambledLetters.value
        val clicked = _clickedLetterIndices.value
        if (index in scrambled.indices && index !in clicked) {
            _clickedLetterIndices.value = clicked + index
            _activeInput.value = _activeInput.value + scrambled[index]
            _isErrorShake.value = false
        }
    }

    fun deselectLastLetter() {
        val clicked = _clickedLetterIndices.value
        val currentInput = _activeInput.value
        if (clicked.isNotEmpty() && currentInput.isNotEmpty()) {
            _clickedLetterIndices.value = clicked.dropLast(1)
            _activeInput.value = currentInput.dropLast(1)
        }
    }

    fun clearInput() {
        _clickedLetterIndices.value = emptyList()
        _activeInput.value = ""
    }

    fun shuffleLetters() {
        val currentLetters = _scrambledLetters.value
        if (currentLetters.isNotEmpty()) {
            // Keep status but scramble indices visually
            _scrambledLetters.value = currentLetters.shuffled()
            clearInput()
        }
    }

    fun submitWord() {
        val guess = _activeInput.value.trim().uppercase()
        if (guess.isEmpty()) return

        val level = _currentLevel.value
        val found = _foundWords.value
        val stats = _gameStats.value

        when {
            guess !in level.targetWords -> {
                // Word doesn't belong to target list
                _isErrorShake.value = true
                _activeFeedbackMessage.value = "\"$guess\" is not a hidden word!"
            }
            guess in found -> {
                // Already discovered
                _isErrorShake.value = true
                _activeFeedbackMessage.value = "Already found \"$guess\"!"
            }
            else -> {
                // Success - Correct Word found!
                val points = guess.length * 15
                val updatedFound = found + guess
                _foundWords.value = updatedFound
                _isCorrectSplash.value = true
                _activeFeedbackMessage.value = "Correct! +$points points"

                // Save Level Progress
                viewModelScope.launch {
                    val progressToSave = LevelProgress(
                        levelId = level.id,
                        completed = updatedFound.size == level.targetWords.size,
                        foundWords = updatedFound.joinToString(","),
                        scoreEarned = (updatedFound.size * 15) // incremental calculations
                    )
                    repository.saveLevelProgress(progressToSave)

                    // Save Game Stats
                    val newScore = stats.totalScore + points
                    val newHighScore = if (newScore > stats.highScore) newScore else stats.highScore
                    var currentLevelId = stats.currentLevelId
                    var levelFinished = false

                    if (updatedFound.size == level.targetWords.size) {
                        // All words found! Complete level
                        levelFinished = true
                        _levelCompletedSuccessfully.value = true
                        _activeFeedbackMessage.value = "Marvelous! Level Complete!"
                    }

                    repository.saveGameStats(
                        stats.copy(
                            totalScore = newScore,
                            highScore = newHighScore
                        )
                    )

                    // Reset Splash after animation timeout
                    kotlinx.coroutines.delay(1000)
                    _isCorrectSplash.value = false

                    if (levelFinished) {
                        kotlinx.coroutines.delay(1500)
                        // Progress to next level automatically or loop to 1
                        val nextLevelId = if (currentLevelId < LevelRegistry.levels.size) {
                            currentLevelId + 1
                        } else {
                            1 // Start back from 1 but preserve high score!
                        }
                        repository.saveGameStats(
                            stats.copy(
                                currentLevelId = nextLevelId,
                                totalScore = newScore,
                                highScore = newHighScore,
                                hintsAvailable = stats.hintsAvailable + 1 // Award extra hint for level completion!
                            )
                        )
                    }
                }
            }
        }
        clearInput()
    }

    fun useHint() {
        val level = _currentLevel.value
        val found = _foundWords.value
        val stats = _gameStats.value

        if (stats.hintsAvailable <= 0) {
            _activeFeedbackMessage.value = "No hints remaining! Complete levels to get more."
            return
        }

        // Find the first unrevealed word
        val unrevealedWords = level.targetWords.filter { it !in found }
        if (unrevealedWords.isNotEmpty()) {
            val hintWord = unrevealedWords.random()
            val points = hintWord.length * 5 // hints award partial score
            val updatedFound = found + hintWord
            _foundWords.value = updatedFound
            _isCorrectSplash.value = true
            _activeFeedbackMessage.value = "Revealed: $hintWord!"

            viewModelScope.launch {
                // Save progress
                val progressToSave = LevelProgress(
                    levelId = level.id,
                    completed = updatedFound.size == level.targetWords.size,
                    foundWords = updatedFound.joinToString(","),
                    scoreEarned = (updatedFound.size * 15)
                )
                repository.saveLevelProgress(progressToSave)

                // Update Stats
                val newScore = stats.totalScore + points
                val newHighScore = if (newScore > stats.highScore) newScore else stats.highScore
                var levelFinished = false

                if (updatedFound.size == level.targetWords.size) {
                    levelFinished = true
                    _levelCompletedSuccessfully.value = true
                    _activeFeedbackMessage.value = "Great Job! Level Complete!"
                }

                repository.saveGameStats(
                     stats.copy(
                         totalScore = newScore,
                         highScore = newHighScore,
                         hintsAvailable = stats.hintsAvailable - 1
                     )
                )

                kotlinx.coroutines.delay(1000)
                _isCorrectSplash.value = false

                if (levelFinished) {
                    kotlinx.coroutines.delay(1500)
                    val nextLevelId = if (stats.currentLevelId < LevelRegistry.levels.size) {
                        stats.currentLevelId + 1
                    } else {
                        1
                    }
                    repository.saveGameStats(
                        stats.copy(
                            currentLevelId = nextLevelId,
                            totalScore = newScore,
                            highScore = newHighScore,
                            hintsAvailable = stats.hintsAvailable // consumed hint is offset
                        )
                    )
                }
            }
        }
    }

    fun showClue(word: String) {
        val level = _currentLevel.value
        val meaning = level.wordClues[word] ?: "A valid mystery word!"
        _selectedWordClue.value = word to meaning
    }

    fun hideClue() {
        _selectedWordClue.value = null
    }

    fun resetWholeGame() {
        viewModelScope.launch {
            repository.resetGame()
            val firstLevel = LevelRegistry.levels.first()
            _currentLevel.value = firstLevel
            resetInputAndScramble(firstLevel)
        }
    }
}

class GameViewModelFactory(private val repository: GameRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
