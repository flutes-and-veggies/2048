package com.packtpub.a2048.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.packtpub.a2048.ui.theme.LocalBoardColors

@Composable
fun WinScreen(
    keepGoingCallback: () -> Unit,
    tryAgainCallbacK: () -> Unit
) {
    val boardColors = LocalBoardColors.current
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
        ) {
            Text(
                "You win!",
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.heightIn(16.dp))
            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                ActionButton(
                    "Keep going",
                    boardColors.primaryButtonBackground,
                    keepGoingCallback
                )
                Spacer(modifier = Modifier.widthIn(16.dp))
                ActionButton(
                    "Try again",
                    boardColors.tile16Background,
                    tryAgainCallbacK
                )
            }
        }
    }
}