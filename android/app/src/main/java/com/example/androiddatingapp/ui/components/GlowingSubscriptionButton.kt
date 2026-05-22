package com.example.androiddatingapp.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.example.androiddatingapp.ui.theme.AppRed
import com.example.androiddatingapp.ui.theme.AppRedDark
import com.example.androiddatingapp.ui.theme.AppRedLight

@Composable
fun GlowingSubscriptionButton(
    remainingLikes: Int,
    onClick: () -> Unit,
    scaleDp: (Float) -> Dp,
    scaleSp: (Float) -> TextUnit,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "subscriptionGlow")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glowPulse",
    )
    val borderPulse by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "borderPulse",
    )

    val shape = RoundedCornerShape(scaleDp(18f))
    val glowAlpha = 0.22f + 0.38f * glowPulse

    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    color = AppRedLight.copy(alpha = glowAlpha),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(scaleDp(20f).toPx()),
                    size = size.copy(
                        width = size.width + scaleDp(6f).toPx(),
                        height = size.height + scaleDp(6f).toPx(),
                    ),
                    topLeft = androidx.compose.ui.geometry.Offset(
                        -scaleDp(3f).toPx(),
                        -scaleDp(3f).toPx(),
                    ),
                )
            }
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        AppRed.copy(alpha = 0.92f + 0.08f * glowPulse),
                        AppRedDark,
                    ),
                ),
            )
            .border(
                width = scaleDp(1.5f),
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.25f + 0.35f * borderPulse),
                        AppRedLight.copy(alpha = 0.5f + 0.5f * borderPulse),
                    ),
                ),
                shape = shape,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = scaleDp(16f), vertical = scaleDp(14f)),
        contentAlignment = Alignment.CenterStart,
    ) {
        Column {
            Text(
                text = "🔥 Подписка на лайки",
                fontSize = scaleSp(16f),
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Spacer(Modifier.height(scaleDp(4f)))
            Text(
                text = if (remainingLikes > 0) {
                    "Осталось $remainingLikes лайков · купить ещё"
                } else {
                    "Лайки закончились · купить 10, 20 или 50"
                },
                fontSize = scaleSp(12f),
                color = Color.White.copy(alpha = 0.9f),
            )
        }
    }
}
