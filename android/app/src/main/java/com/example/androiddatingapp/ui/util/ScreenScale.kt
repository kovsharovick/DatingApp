package com.example.androiddatingapp.ui.util

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androiddatingapp.ui.model.ScreenInfo

data class ScreenScale(
    val scale: Float,
    val dp: (Float) -> Dp,
    val sp: (Float) -> TextUnit
)

fun rememberScreenScale(screen: ScreenInfo): ScreenScale {
    val widthDp = screen.widthPx / screen.density
    val heightDp = screen.heightPx / screen.density

    val scale = minOf(widthDp / 360f, heightDp / 800f).coerceIn(0.85f, 1.4f)
    return ScreenScale(
        scale = scale,
        dp = { v -> (v * scale).dp },
        sp = { v -> (v * scale).sp }
    )
}

