package com.example.androiddatingapp.ui.components

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import com.example.androiddatingapp.R
import com.example.androiddatingapp.data.MediaUrlResolver
import com.example.androiddatingapp.data.api.ApiClient

@Composable
fun VideoPlayerView(
    videoUrl: String,
    modifier: Modifier = Modifier,
    playWhenReady: Boolean = true,
    repeat: Boolean = true,
) {
    val resolvedUrl = remember(videoUrl) { MediaUrlResolver.resolve(videoUrl) }
    if (resolvedUrl.isBlank()) return

    val context = LocalContext.current
    var playbackError by remember(resolvedUrl) { mutableStateOf<String?>(null) }

    val exoPlayer: ExoPlayer = remember(resolvedUrl) {
        val dataSourceFactory = OkHttpDataSource.Factory(ApiClient.mediaHttpClient)
        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(dataSourceFactory)
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
            .apply {
                setMediaItem(MediaItem.fromUri(resolvedUrl))
                prepare()
                this.playWhenReady = playWhenReady
                repeatMode = if (repeat) {
                    Player.REPEAT_MODE_ONE
                } else {
                    Player.REPEAT_MODE_OFF
                }
                addListener(object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        playbackError = error.message ?: "Ошибка воспроизведения"
                    }
                })
            }
    }

    DisposableEffect(resolvedUrl) {
        onDispose { exoPlayer.release() }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        if (playbackError == null) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    (LayoutInflater.from(ctx).inflate(R.layout.video_player_view, null, false) as PlayerView).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        )
                        player = exoPlayer
                    }
                },
                update = { playerView ->
                    playerView.player = exoPlayer
                },
            )
        }
        playbackError?.let {
            Text(
                text = "Видео не загрузилось.\nПроверьте туннель MinIO.",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 13.sp,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
