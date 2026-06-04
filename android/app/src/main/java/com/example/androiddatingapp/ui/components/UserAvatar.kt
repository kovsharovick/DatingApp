package com.example.androiddatingapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.androiddatingapp.data.MediaUrlResolver
import com.example.androiddatingapp.ui.model.Gender
import com.example.androiddatingapp.ui.theme.AppBlue
import com.example.androiddatingapp.ui.theme.AppBlueLight
import com.example.androiddatingapp.ui.theme.AppRed
import com.example.androiddatingapp.ui.theme.AppRedLight

@Composable
fun UserAvatar(
    name: String,
    avatarUrl: String,
    size: Dp,
    scaleSp: (Float) -> TextUnit,
    modifier: Modifier = Modifier,
    gender: Gender? = null,
    onClick: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val isLocalFile = avatarUrl.startsWith("file://") || avatarUrl.startsWith("content://")
    val resolvedUrl = remember(avatarUrl) {
        if (isLocalFile) avatarUrl else MediaUrlResolver.resolve(avatarUrl)
    }
    var imageFailed by remember(resolvedUrl) { mutableStateOf(false) }

    val (bg, letterColor) = when (gender) {
        Gender.MALE -> AppBlueLight.copy(alpha = 0.35f) to AppBlue
        Gender.FEMALE -> AppRedLight.copy(alpha = 0.35f) to AppRed
        null -> MaterialTheme.colorScheme.primary.copy(alpha = 0.22f) to MaterialTheme.colorScheme.primary
    }

    val showLetter = resolvedUrl.isBlank() || imageFailed

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bg)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        if (!showLetter) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(resolvedUrl)
                    .crossfade(true)
                    .listener(
                        onError = { _, _ -> imageFailed = true },
                        onSuccess = { _, _ -> imageFailed = false },
                    )
                    .build(),
                imageLoader = AppImageLoader.get(context),
                contentDescription = name,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
        }
        if (showLetter) {
            Text(
                text = name.trim().take(1).uppercase(),
                fontSize = scaleSp(size.value * 0.32f),
                fontWeight = FontWeight.Bold,
                color = letterColor,
            )
        }
    }
}
