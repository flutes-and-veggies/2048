package com.packtpub.a2048.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.packtpub.a2048.MainActivity.Companion.MAIN_AXIS_THRESHOLD
import com.packtpub.a2048.MainActivity.Companion.SECONDARY_AXIS_THRESHOLD
import com.packtpub.a2048.mobius.GameEvent
import com.packtpub.a2048.mobius.GameModel
import com.packtpub.a2048.ui.theme.AppTheme
import com.packtpub.a2048.ui.theme.LocalBoardColors
import kotlinx.coroutines.launch

@Composable
fun App(
    model: GameModel,
    dispatchEvent: (GameEvent) -> Unit,
) {
    val boardColors = LocalBoardColors.current
    var dragged by remember { mutableStateOf(false) }
    val score = model.score
    val bestScore = model.bestScore
    val addedScore = model.addedScore

    var hasSeenScore by rememberSaveable { mutableStateOf(false) }
    var isLoseScreenShowing by rememberSaveable { mutableStateOf(model.hasLost) }
    var isWinScreenShowing by rememberSaveable { mutableStateOf(false) }

    var canShowWinScreen by rememberSaveable { mutableStateOf(true) }
    val animationSpec = tween<Float>(durationMillis = 300)

    LaunchedEffect(model.hasLost) {
        isLoseScreenShowing = model.hasLost
    }

    LaunchedEffect(score) {
        if (!hasSeenScore) {
            hasSeenScore = true
        } else {
            if (score >= 144) {
                isWinScreenShowing = true
            } else if (score == 0 && !canShowWinScreen) {
                canShowWinScreen = true
                isWinScreenShowing = false
            }
        }
    }

    LaunchedEffect(addedScore) {
        launch {
            addedScore.opacity.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 300)
            )
            addedScore.opacity.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 1200)
            )
        }
        launch {
            addedScore.offset.animateTo(
                targetValue = -35f,
                animationSpec = tween(durationMillis = 1500)
            )
        }
    }

    AppTheme {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { _ ->
                            dragged = false
                        },
                        onDragEnd = {
                            dragged = false
                        },
                        onDrag = { change, dragAmount ->
                            val (dx, dy) = dragAmount
                            if (dragged || (isWinScreenShowing && canShowWinScreen)) {
                                return@detectDragGestures
                            }
                            if (
                                dx >= MAIN_AXIS_THRESHOLD && Math.abs(dy) <= SECONDARY_AXIS_THRESHOLD
                            ) {
                                dispatchEvent(GameEvent.SwipeRight)
                                dragged = true
                            } else if (
                                dx <= -MAIN_AXIS_THRESHOLD && Math.abs(dy) <= SECONDARY_AXIS_THRESHOLD
                            ) {
                                dispatchEvent(GameEvent.SwipeLeft)
                                dragged = true
                            } else if (
                                dy >= MAIN_AXIS_THRESHOLD && Math.abs(dx) <= SECONDARY_AXIS_THRESHOLD
                            ) {
                                dispatchEvent(GameEvent.SwipeDown)
                                dragged = true
                            } else if (
                                dy <= -MAIN_AXIS_THRESHOLD && Math.abs(dx) <= SECONDARY_AXIS_THRESHOLD
                            ) {
                                dispatchEvent(GameEvent.SwipeUp)
                                dragged = true
                            }
                        }
                    )
                }
        ) { innerPadding ->
            Box(
                modifier = Modifier.padding(innerPadding)
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).offset(y = 32.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            NumberWithLabel(label = "SCORE", number = score)
                            if (addedScore.value > 0) {
                                Text(
                                    text = "+${addedScore.value}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 28.sp,
                                    color = boardColors.tileLowText,
                                    modifier = Modifier
                                        .offset(y = addedScore.offset.value.dp)
                                        .alpha(addedScore.opacity.value)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        NumberWithLabel(label = "BEST", number = bestScore)
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    ActionButton(
                        "New Game",
                        action = {
                            dispatchEvent(GameEvent.StartGame)
                        }
                    )
                }
                Board(model)
            }
        }
        AnimatedVisibility(
            visible = isLoseScreenShowing,
            enter = fadeIn(animationSpec = animationSpec),
            exit = fadeOut(animationSpec = animationSpec),
        ) {
            GameOverScreen(
                tryAgainCallback = {
                    dispatchEvent(GameEvent.StartGame)
                }
            )
        }
        AnimatedVisibility(
            visible = isWinScreenShowing && canShowWinScreen,
            enter = fadeIn(animationSpec = animationSpec),
            exit = fadeOut(animationSpec = animationSpec),
        ) {
            WinScreen(
                keepGoingCallback = {
                    isWinScreenShowing = false
                    canShowWinScreen = false
                },
                tryAgainCallbacK = {
                    dispatchEvent(GameEvent.StartGame)
                    isWinScreenShowing = false
                }
            )
        }
    }
}