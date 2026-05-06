package com.example.androiddatingapp.ui.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.example.androiddatingapp.ui.model.ProfileUi
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    isProfileActive: Boolean,
    onOpenSettings: () -> Unit,
    scaleDp: (Float) -> Dp,
    scaleSp: (Float) -> TextUnit,
    modifier: Modifier = Modifier
) {
    val profiles = remember {
        listOf(
            ProfileUi(name = "Анастасия", age = 23, city = "Москва"),
            ProfileUi(name = "София", age = 22, city = "Санкт‑Петербург"),
            ProfileUi(name = "Мария", age = 25, city = "Казань"),
            ProfileUi(name = "Екатерина", age = 24, city = "Новосибирск")
        )
    }
    var currentProfileIndex by remember { mutableIntStateOf(0) }
    val currentProfile = profiles[currentProfileIndex % profiles.size]

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = scaleDp(12f), vertical = scaleDp(10f))
    ) {
        SwipeableVideoCard(
            profile = currentProfile,
            enabled = isProfileActive,
            scaleDp = scaleDp,
            scaleSp = scaleSp,
            modifier = Modifier.fillMaxSize(),
            onDislike = { currentProfileIndex = (currentProfileIndex + 1) % profiles.size },
            onLike = { currentProfileIndex = (currentProfileIndex + 1) % profiles.size }
        )

        if (!isProfileActive) {
            PausedOverlay(
                scaleDp = scaleDp,
                scaleSp = scaleSp,
                onOpenSettings = onOpenSettings,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun SwipeableVideoCard(
    profile: ProfileUi,
    enabled: Boolean,
    scaleDp: (Float) -> Dp,
    scaleSp: (Float) -> TextUnit,
    onDislike: () -> Unit,
    onLike: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    val offsetXPx = remember { Animatable(0f) }
    var isAnimatingOut by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(scaleDp(18f)))
            .background(Color(0xFF101827))
    ) {
        val cardWidthPx = with(density) { maxWidth.toPx() }
        val actionThresholdPx = cardWidthPx * 0.28f
        val progress = (kotlin.math.abs(offsetXPx.value) / actionThresholdPx).coerceIn(0f, 1.2f)
        val isLike = offsetXPx.value > 0f
        val showAction = progress > 0.02f

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(isAnimatingOut, enabled) {
                    if (!enabled) return@pointerInput
                    detectDragGestures(
                        onDragCancel = {
                            scope.launch {
                                offsetXPx.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                )
                            }
                        },
                        onDragEnd = {
                            val finalX = offsetXPx.value
                            val action =
                                if (finalX >= actionThresholdPx) "LIKE"
                                else if (finalX <= -actionThresholdPx) "NOPE"
                                else null

                            if (action == null) {
                                scope.launch {
                                    offsetXPx.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                    )
                                }
                            } else {
                                isAnimatingOut = true
                                val target = if (action == "LIKE") cardWidthPx * 1.2f else -cardWidthPx * 1.2f
                                scope.launch {
                                    offsetXPx.animateTo(
                                        targetValue = target,
                                        animationSpec = spring(stiffness = Spring.StiffnessMedium)
                                    )
                                    if (action == "LIKE") onLike() else onDislike()
                                    offsetXPx.snapTo(0f)
                                    isAnimatingOut = false
                                }
                            }
                        },
                        onDrag = { change, dragAmount ->
                            if (isAnimatingOut) return@detectDragGestures
                            change.consume()
                            val next = (offsetXPx.value + dragAmount.x)
                                .coerceIn(-cardWidthPx * 0.7f, cardWidthPx * 0.7f)
                            scope.launch { offsetXPx.snapTo(next) }
                        }
                    )
                }
                .graphicsLayer { translationX = offsetXPx.value }
        ) {
            // Видео (заглушка)
            Text(
                text = "Видео анкета (заглушка)",
                color = Color.White,
                fontSize = scaleSp(14f),
                modifier = Modifier.padding(scaleDp(14f))
            )

            ProfileInfoStrip(
                profile = profile,
                scaleDp = scaleDp,
                scaleSp = scaleSp,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(scaleDp(12f))
            )
        }

        if (enabled && showAction) {
            val actionColor = if (isLike) Color(0xFF22C55E) else Color(0xFFEF4444)
            val alpha = (0.20f + 0.80f * progress).coerceIn(0f, 1.0f)
            val edgeWidth = scaleDp(84f)

            val align = if (isLike) Alignment.CenterEnd else Alignment.CenterStart
            val brush =
                if (isLike) {
                    Brush.horizontalGradient(
                        colorStops = arrayOf(
                            0.00f to Color.Transparent,
                            0.35f to actionColor.copy(alpha = (alpha * 0.12f).coerceIn(0f, 1f)),
                            0.65f to actionColor.copy(alpha = (alpha * 0.30f).coerceIn(0f, 1f)),
                            0.85f to actionColor.copy(alpha = (alpha * 0.55f).coerceIn(0f, 1f)),
                            1.00f to actionColor.copy(alpha = alpha)
                        )
                    )
                } else {
                    Brush.horizontalGradient(
                        colorStops = arrayOf(
                            0.00f to actionColor.copy(alpha = alpha),
                            0.15f to actionColor.copy(alpha = (alpha * 0.55f).coerceIn(0f, 1f)),
                            0.35f to actionColor.copy(alpha = (alpha * 0.30f).coerceIn(0f, 1f)),
                            0.65f to actionColor.copy(alpha = (alpha * 0.12f).coerceIn(0f, 1f)),
                            1.00f to Color.Transparent
                        )
                    )
                }

            Box(
                modifier = Modifier
                    .align(align)
                    .fillMaxHeight()
                    .width(edgeWidth)
                    .background(brush)
            )
        }
    }
}

@Composable
private fun PausedOverlay(
    scaleDp: (Float) -> Dp,
    scaleSp: (Float) -> TextUnit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable(enabled = true, onClick = { /* consume */ }),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(scaleDp(16f))
                .clip(RoundedCornerShape(scaleDp(18f)))
                .background(Color(0xFF101827))
                .padding(scaleDp(16f)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Анкета на паузе",
                fontSize = scaleSp(16f),
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Spacer(Modifier.height(scaleDp(8f)))
            Text(
                text = "Возобнови показ анкеты в настройках, чтобы снова листать ленту.",
                fontSize = scaleSp(13f),
                color = Color.White.copy(alpha = 0.85f)
            )
            Spacer(Modifier.height(scaleDp(12f)))
            Button(
                onClick = onOpenSettings,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                contentPadding = PaddingValues(horizontal = scaleDp(14f), vertical = scaleDp(10f))
            ) {
                Text(text = "Перейти в настройки", fontSize = scaleSp(13f), color = Color.White)
            }
        }
    }
}

@Composable
private fun ProfileInfoStrip(
    profile: ProfileUi,
    scaleDp: (Float) -> Dp,
    scaleSp: (Float) -> TextUnit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .clip(RoundedCornerShape(scaleDp(16f)))
            .background(Color.Black.copy(alpha = 0.38f))
            .padding(horizontal = scaleDp(14f), vertical = scaleDp(12f)),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = "${profile.name}, ${profile.age}",
                fontSize = scaleSp(18f),
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Spacer(Modifier.height(scaleDp(4f)))
            Text(
                text = profile.city,
                fontSize = scaleSp(14f),
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

