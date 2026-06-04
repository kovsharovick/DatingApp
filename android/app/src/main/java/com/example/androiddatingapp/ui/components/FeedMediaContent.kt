package com.example.androiddatingapp.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.TextUnit
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.androiddatingapp.data.MediaUrlResolver
import com.example.androiddatingapp.ui.model.ProfileUi

@Composable
fun FeedMediaContent(
    profile: ProfileUi,
    scaleSp: (Float) -> TextUnit,
    modifier: Modifier = Modifier,
    playWhenReady: Boolean = true,
) {
    val videoUrl = MediaUrlResolver.resolve(profile.videoUrl)
    val thumbnailUrl = MediaUrlResolver.resolve(profile.thumbnailUrl)
    val avatarUrl = MediaUrlResolver.resolve(profile.avatarUrl)

    when {
        videoUrl.isNotBlank() -> {
            VideoPlayerView(
                videoUrl = videoUrl,
                modifier = modifier,
                playWhenReady = playWhenReady,
            )
        }
        thumbnailUrl.isNotBlank() -> {
            RemoteFeedImage(
                url = thumbnailUrl,
                contentDescription = profile.name,
                modifier = modifier,
            )
        }
        avatarUrl.isNotBlank() -> {
            RemoteFeedImage(
                url = avatarUrl,
                contentDescription = profile.name,
                modifier = modifier,
                contentScale = ContentScale.Crop,
            )
        }
        else -> {
            Box(modifier = modifier, contentAlignment = Alignment.Center) {
                Text(
                    text = "Медиа недоступно",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = scaleSp(14f),
                )
            }
        }
    }
}

@Composable
private fun RemoteFeedImage(
    url: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
) {
    val context = LocalContext.current
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(url)
            .crossfade(true)
            .build(),
        imageLoader = AppImageLoader.get(context),
        contentDescription = contentDescription,
        modifier = modifier.fillMaxSize(),
        contentScale = contentScale,
    )
}
