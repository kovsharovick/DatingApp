package com.example.androiddatingapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import com.example.androiddatingapp.ui.theme.AppButtonDefaults
import com.example.androiddatingapp.ui.theme.DarkCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.foundation.layout.Box

@Composable
fun FeatureBlockOverlay(
    title: String,
    message: String,
    actionText: String,
    onAction: () -> Unit,
    scaleDp: (Float) -> Dp,
    scaleSp: (Float) -> TextUnit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable(enabled = true, onClick = { /* consume */ }),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(scaleDp(16f))
                .clip(RoundedCornerShape(scaleDp(18f)))
                .background(DarkCard)
                .padding(scaleDp(16f)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = scaleSp(16f),
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Spacer(Modifier.height(scaleDp(8f)))
            Text(
                text = message,
                fontSize = scaleSp(13f),
                color = Color.White.copy(alpha = 0.85f)
            )
            Spacer(Modifier.height(scaleDp(12f)))
            Button(
                onClick = onAction,
                colors = AppButtonDefaults.blue(),
                contentPadding = PaddingValues(horizontal = scaleDp(14f), vertical = scaleDp(10f))
            ) {
                Text(text = actionText, fontSize = scaleSp(13f), color = Color.White)
            }
        }
    }
}
