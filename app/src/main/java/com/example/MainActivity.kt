package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.database.WordCraftDatabase
import com.example.data.model.GameLevel
import com.example.data.model.LevelRegistry
import com.example.data.repository.GameRepository
import com.example.ui.theme.*
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.GameViewModelFactory
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val context = LocalContext.current
                val database = remember { WordCraftDatabase.getDatabase(context) }
                val repository = remember { GameRepository(database.gameDao()) }
                val factory = remember { GameViewModelFactory(repository) }
                val viewmodel: GameViewModel = viewModel(factory = factory)

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = SlateDarkBg
                ) { innerPadding ->
                    WordCraftGameScreen(
                        viewModel = viewmodel,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun WordCraftGameScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.gameStatsState.collectAsStateWithLifecycle()
    val currentLevel by viewModel.currentLevelState.collectAsStateWithLifecycle()
    val scrambledLetters by viewModel.scrambledLettersState.collectAsStateWithLifecycle()
    val activeInput by viewModel.activeInputState.collectAsStateWithLifecycle()
    val clickedIndices by viewModel.clickedLetterIndicesState.collectAsStateWithLifecycle()
    val foundWords by viewModel.foundWordsState.collectAsStateWithLifecycle()

    // Animation / Feedback flows
    val isCorrectSplash by viewModel.isCorrectSplashState.collectAsStateWithLifecycle()
    val isErrorShake by viewModel.isErrorShakeState.collectAsStateWithLifecycle()
    val feedbackMessage by viewModel.activeFeedbackMessageState.collectAsStateWithLifecycle()
    val selectedClue by viewModel.selectedWordClueState.collectAsStateWithLifecycle()
    val levelCompleted by viewModel.levelCompletedSuccessfullyState.collectAsStateWithLifecycle()

    // Double check dialogue for resetting game
    var showResetDialog by remember { mutableStateOf(false) }

    // Double check dialogue for help instructions
    var showHelpDialog by remember { mutableStateOf(false) }

    // UI Structure
    Box(
        modifier = modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(SlateDarkBg, Color(0xFF030712))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // --- HEADER ROW ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { showResetDialog = true },
                    modifier = Modifier
                        .testTag("reset_game_icon_button")
                        .background(SlateSurface, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Game Progress",
                        tint = TextSecondary
                    )
                }

                // Branding Logotype
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "WORD CRAFT",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CyberTeal,
                        letterSpacing = 3.sp,
                        fontFamily = FontFamily.SansSerif
                    )
                    Text(
                        text = "Level ${currentLevel.id}: ${currentLevel.name}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = HighlightAmber,
                        fontFamily = FontFamily.SansSerif
                    )
                }

                IconButton(
                    onClick = { showHelpDialog = true },
                    modifier = Modifier
                        .testTag("help_icon_button")
                        .background(SlateSurface, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "How To Play",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- SCORE & STATS PANEL ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Score Box
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("SCORE", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${stats.totalScore}",
                            fontSize = 18.sp,
                            color = TextPrimary,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // Best Score Box
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("BEST", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${stats.highScore}",
                            fontSize = 18.sp,
                            color = TextPrimary,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // Hint Trigger Button (High visual engagement)
                Button(
                    onClick = { viewModel.useHint() },
                    colors = ButtonDefaults.buttonColors(containerColor = SlateSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 6.dp)
                        .testTag("hint_action_button"),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("HINTS", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Use Hint",
                                tint = HighlightAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${stats.hintsAvailable}",
                                fontSize = 16.sp,
                                color = TextPrimary,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- PROGRESS TRACKER (THE GRID OF HIDDEN WORDS) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0x221E293B), RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0x11FFFFFF), RoundedCornerShape(16.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                WordGridContainer(
                    level = currentLevel,
                    foundWords = foundWords,
                    onWordClick = { word -> viewModel.showClue(word) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- ACTIVE FEEDBACK STRIP & DRAFT WRITER ---
            feedbackMessage?.let { msg ->
                Text(
                    text = msg,
                    fontSize = 14.sp,
                    color = if (isErrorShake) ErrorRed else if (isCorrectSplash) GlowingGreen else TextSecondary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- ACTIVE INPUT SPELLED PREVIEW ---
            ActiveDraftPanel(
                draftText = activeInput,
                shakeTrigger = isErrorShake,
                successTrigger = isCorrectSplash
            )

            Spacer(modifier = Modifier.height(12.dp))

            // --- SPELLER ROW ACTIONS & ROTATING LETTER WHEEL ---
            LetterWheelAndActionsLayout(
                letters = scrambledLetters,
                clickedIndices = clickedIndices,
                onLetterClick = { index -> viewModel.selectLetter(index) },
                onBackspace = { viewModel.deselectLastLetter() },
                onClear = { viewModel.clearInput() },
                onSubmit = { viewModel.submitWord() },
                onShuffle = { viewModel.shuffleLetters() }
            )
        }

        // --- EXQUISITE WORD COMPLETE LEVEL SPLASH SCREEN ---
        AnimatedVisibility(
            visible = levelCompleted,
            enter = fadeIn() + scaleIn(initialScale = 0.8f),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xDD0F172A)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                    modifier = Modifier
                        .padding(32.dp)
                        .border(1.dp, CyberTeal, RoundedCornerShape(24.dp)),
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Level Complete",
                            tint = GlowingGreen,
                            modifier = Modifier
                                .size(72.dp)
                                .graphicsLayer {
                                    scaleX = 1.1f
                                    scaleY = 1.1f
                                }
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "LEVEL COMPLETE!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary,
                            fontFamily = FontFamily.SansSerif,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "You found all target words in\n\"${currentLevel.name}\"!",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Next challenge is starting...",
                            fontSize = 12.sp,
                            color = HighlightAmber,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.graphicsLayer {
                                alpha = 0.85f
                            }
                        )
                    }
                }
            }
        }

        // --- REVEALED DICTIONARY GLOSSARY/CLUE POPUP ---
        selectedClue?.let { (word, meaning) ->
            Dialog(onDismissRequest = { viewModel.hideClue() }) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, HighlightAmber.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "DICTIONARY DEFINITION",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = HighlightAmber,
                            letterSpacing = 2.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = word,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = CyberTeal
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = meaning,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = TextPrimary,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { viewModel.hideClue() },
                            colors = ButtonDefaults.buttonColors(containerColor = HighlightAmber),
                            shape = CircleShape,
                            modifier = Modifier.testTag("dismiss_clue_dialog")
                        ) {
                            Text(
                                "Got It",
                                color = SlateDarkBg,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- HOW TO PLAY DIALOG ---
        if (showHelpDialog) {
            Dialog(onDismissRequest = { showHelpDialog = false }) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "HOW TO PLAY",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberTeal
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.Top) {
                                Text("💡", modifier = Modifier.padding(end = 8.dp))
                                Text("Spell valid words of 3, 4, or 5 letters using the Letter Wheel below.", color = TextPrimary, fontSize = 13.sp)
                            }
                            Row(verticalAlignment = Alignment.Top) {
                                Text("💡", modifier = Modifier.padding(end = 8.dp))
                                Text("Tap letters in order to build a word, then press the checkmark button [✓] to submit.", color = TextPrimary, fontSize = 13.sp)
                            }
                            Row(verticalAlignment = Alignment.Top) {
                                Text("💡", modifier = Modifier.padding(end = 8.dp))
                                Text("Need help? Click any empty word box to read its definition, or use a hint!", color = TextPrimary, fontSize = 13.sp)
                            }
                            Row(verticalAlignment = Alignment.Top) {
                                Text("💡", modifier = Modifier.padding(end = 8.dp))
                                Text("Successfully find all words in a level to unlock the next level and get extra points!", color = TextPrimary, fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { showHelpDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberTeal),
                            shape = CircleShape,
                            modifier = Modifier.testTag("dismiss_help_dialog")
                        ) {
                            Text("Ready to Craft", color = SlateDarkBg, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- RESET PROGRESS CONFIRMATION DIALOG ---
        if (showResetDialog) {
            Dialog(onDismissRequest = { showResetDialog = false }) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Confirmation Warning",
                            tint = ErrorRed,
                            modifier = Modifier.size(48.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "RESET PROGRESS?",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "This will return you back to level 1 and erase your current score. Are you sure?",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            OutlinedButton(
                                onClick = { showResetDialog = false },
                                border = BorderStroke(1.dp, TextSecondary),
                                shape = CircleShape
                            ) {
                                Text("Cancel", color = TextSecondary)
                            }

                            Button(
                                onClick = {
                                    showResetDialog = false
                                    viewModel.resetWholeGame()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                                shape = CircleShape,
                                modifier = Modifier.testTag("confirm_reset_button")
                            ) {
                                Text("Reset Game", color = TextPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

    }
}

@Composable
fun WordGridContainer(
    level: GameLevel,
    foundWords: Set<String>,
    onWordClick: (String) -> Unit
) {
    // Scrollable container for target words
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Group words by length, so we arrange them sorted and cleanly
        val wordsByLength = level.targetWords.groupBy { it.length }.toSortedMap()

        wordsByLength.forEach { (length, words) ->
            Text(
                text = "$length-LETTER WORDS",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )

            // Chunk words into smaller sub-rows to avoid experimental FlowRow or offscreen cuts
            val chunkedRows = words.chunked(3)
            chunkedRows.forEach { rowWords ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    rowWords.forEach { word ->
                        val isFound = word in foundWords
                        TargetWordSlot(
                            word = word,
                            isFound = isFound,
                            onClick = { onWordClick(word) }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
fun TargetWordSlot(
    word: String,
    isFound: Boolean,
    onClick: () -> Unit
) {
    // Scale animation state when found
    val revealScale by animateFloatAsState(
        targetValue = if (isFound) 1.05f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    // Container showing individual square block slots
    Row(
        modifier = Modifier
            .clickable(
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = null // Quiet click to show dictionary clue hint
            )
            .graphicsLayer {
                scaleX = revealScale
                scaleY = revealScale
            },
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
    ) {
        word.forEach { char ->
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (isFound) GlowingGreen.copy(alpha = 0.9f) else SlateSurface
                    )
                    .border(
                        width = 1.dp,
                        color = if (isFound) GlowingGreen else Color(0x33FFFFFF),
                        shape = RoundedCornerShape(4.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isFound) char.toString() else "",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = SlateDarkBg,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun ActiveDraftPanel(
    draftText: String,
    shakeTrigger: Boolean,
    successTrigger: Boolean
) {
    // Custom spring shake modifier mechanics
    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(shakeTrigger) {
        if (shakeTrigger) {
            repeat(4) {
                shakeOffset.animateTo(12f, animationSpec = spring(stiffness = Spring.StiffnessHigh))
                shakeOffset.animateTo(-12f, animationSpec = spring(stiffness = Spring.StiffnessHigh))
            }
            shakeOffset.animateTo(0f)
        }
    }

    // Success glow factor mechanics
    val glowColor by animateColorAsState(
        targetValue = if (successTrigger) GlowingGreen else CyberTeal,
        animationSpec = tween(durationMillis = 200)
    )

    val scaleFactor by animateFloatAsState(
        targetValue = if (successTrigger) 1.15f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .graphicsLayer {
                translationX = shakeOffset.value
                scaleX = scaleFactor
                scaleY = scaleFactor
            },
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (successTrigger) GlowingGreen.copy(alpha = 0.15f) else Color(0x1A06B6D4)
            ),
            border = BorderStroke(
                width = 2.dp,
                color = glowColor
            ),
            modifier = Modifier
                .height(56.dp)
                .fillMaxWidth()
                .shadow(
                    elevation = if (successTrigger) 12.dp else 2.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = glowColor,
                    spotColor = glowColor
                )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (draftText.isEmpty()) {
                    Text(
                        text = "Craft a word...",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary.copy(alpha = 0.5f)
                    )
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        draftText.forEach { char ->
                            Text(
                                text = char.toString(),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LetterWheelAndActionsLayout(
    letters: List<Char>,
    clickedIndices: List<Int>,
    onLetterClick: (Int) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onSubmit: () -> Unit,
    onShuffle: () -> Unit
) {
    // Layout wheel inside a Row or Column to optimize touch target densities
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- LEFT UTILITY COLUMN ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // BACKSPACE/ERASE LAST BUTTON
            IconButton(
                onClick = onBackspace,
                modifier = Modifier
                    .size(48.dp)
                    .background(SlateSurface, CircleShape)
                    .testTag("backspace_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Erase last letter",
                    tint = TextPrimary
                )
            }

            // CLEAR CURRENT DRAFT BUTTON
            IconButton(
                onClick = onClear,
                modifier = Modifier
                    .size(48.dp)
                    .background(SlateSurface, CircleShape)
                    .testTag("clear_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear spelled word",
                    tint = ErrorRed
                )
            }
        }

        // --- CENTER INTERACTIVE TRIGONOMETRIC CIRCULAR WHEEL ---
        Box(
            modifier = Modifier
                .size(220.dp)
                .background(Color(0x1F475569), CircleShape)
                .border(2.dp, Color(0x1AFFFFFF), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            val count = letters.size
            if (count > 0) {
                // Arrangement Radius
                val radius = 72.dp

                letters.forEachIndexed { index, char ->
                    val isUsed = index in clickedIndices

                    // Distribute evenly along perimeter
                    val angleRad = (2.0 * java.lang.Math.PI / count) * index - (java.lang.Math.PI / 2.0)
                    val xOffset = radius * cos(angleRad).toFloat()
                    val yOffset = radius * sin(angleRad).toFloat()

                    // Visual feedback
                    val scale by animateFloatAsState(
                        targetValue = if (isUsed) 0.85f else 1.0f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                    )

                    val containerColor by animateColorAsState(
                        targetValue = if (isUsed) WheelButtonSelected else WheelButtonBg
                    )

                    val textColor by animateColorAsState(
                        targetValue = if (isUsed) TextSecondary else TextPrimary
                    )

                    Box(
                        modifier = Modifier
                            .offset(x = xOffset, y = yOffset)
                            .size(48.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .clip(CircleShape)
                            .background(containerColor)
                            .clickable(
                                enabled = !isUsed,
                                onClick = { onLetterClick(index) }
                            )
                            .border(
                                width = 1.6.dp,
                                color = if (isUsed) Color.Transparent else CyberTeal.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                            .testTag("wheel_char_${index}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char.toString(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = textColor
                        )
                    }
                }
            }

            // Central Shuffle Button inside the wheel
            IconButton(
                onClick = onShuffle,
                modifier = Modifier
                    .size(52.dp)
                    .background(SlateDarkBg, CircleShape)
                    .border(1.6.dp, HighlightAmber.copy(alpha = 0.8f), CircleShape)
                    .shadow(4.dp, CircleShape)
                    .testTag("shuffle_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow, // represents shuffle/scrambling play
                    contentDescription = "Shuffle Letters Position",
                    tint = HighlightAmber,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(135f) // makes the diagonal/recursive arrow effect
                )
            }
        }

        // --- RIGHT ACTION COLUMN ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ENTER/SUBMIT BUTTON (Key feature, green glowing highlight)
            IconButton(
                onClick = onSubmit,
                modifier = Modifier
                    .size(56.dp)
                    .background(GlowingGreen, CircleShape)
                    .shadow(8.dp, CircleShape, spotColor = GlowingGreen)
                    .testTag("submit_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Submit guessed word",
                    tint = SlateDarkBg,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}
