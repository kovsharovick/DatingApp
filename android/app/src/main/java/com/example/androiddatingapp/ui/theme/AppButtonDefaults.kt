package com.example.androiddatingapp.ui.theme

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object AppButtonDefaults {
    @Composable
    fun blue(): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = AppBlue,
        contentColor = Color.White,
        disabledContainerColor = AppBlue.copy(alpha = 0.35f),
        disabledContentColor = Color.White.copy(alpha = 0.6f),
    )

    @Composable
    fun red(): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = AppRed,
        contentColor = Color.White,
        disabledContainerColor = AppRed.copy(alpha = 0.35f),
        disabledContentColor = Color.White.copy(alpha = 0.6f),
    )

    @Composable
    fun outlinedBlue() = ButtonDefaults.outlinedButtonColors(
        contentColor = AppBlueLight,
    )

    @Composable
    fun outlinedRed() = ButtonDefaults.outlinedButtonColors(
        contentColor = AppRedLight,
    )

    @Composable
    fun outlinedMuted() = ButtonDefaults.outlinedButtonColors(
        contentColor = DarkOnSurfaceMuted,
        disabledContentColor = DarkOnSurfaceMuted.copy(alpha = 0.45f),
    )
}
