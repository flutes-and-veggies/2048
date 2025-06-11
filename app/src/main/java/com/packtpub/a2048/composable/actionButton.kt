package com.packtpub.a2048.composable

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.packtpub.a2048.ui.theme.LocalBoardColors

@Composable
fun ActionButton(
    label: String,
    color: Color = LocalBoardColors.current.secondaryButtonBackground,
    action: () -> Unit,
) {
    Button(
        elevation = ButtonDefaults.elevatedButtonElevation(),
        modifier = Modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = color
        ),
        shape = RoundedCornerShape(4.dp),
        onClick = { action() }
    ) {
        Text(
            text = label,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}