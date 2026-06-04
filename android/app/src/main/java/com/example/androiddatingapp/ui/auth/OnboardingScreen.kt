package com.example.androiddatingapp.ui.auth

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import com.example.androiddatingapp.ui.theme.AppButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.example.androiddatingapp.ui.components.launchVideoPicker
import com.example.androiddatingapp.ui.components.rememberVideoPicker
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    userName: String,
    hasVideoAlready: Boolean,
    onUploadVideo: suspend (Uri) -> Result<Unit>,
    onComplete: () -> Unit,
    onSkip: () -> Unit,
    scaleDp: (Float) -> Dp,
    scaleSp: (Float) -> TextUnit,
    modifier: Modifier = Modifier,
) {
    var videoUploaded by remember { mutableStateOf(hasVideoAlready) }
    var uploading by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val videoPicker = rememberVideoPicker { uri ->
        scope.launch {
            uploading = true
            uploadError = null
            onUploadVideo(uri)
                .onSuccess { videoUploaded = true }
                .onFailure { uploadError = it.message }
            uploading = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = scaleDp(20f), vertical = scaleDp(24f))
    ) {
        Text(
            text = "Привет, $userName!",
            fontSize = scaleSp(24f),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(scaleDp(8f)))
        Text(
            text = "Добавьте видео для анкеты. Без видео лента будет недоступна, чаты останутся открытыми. " +
                "Описание «О себе» можно заполнить позже в профиле.",
            fontSize = scaleSp(14f),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
        )
        Spacer(Modifier.height(scaleDp(20f)))

        Text(
            text = "Видео анкеты",
            fontSize = scaleSp(16f),
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(scaleDp(8f)))
        Text(
            text = when {
                uploading -> "Загрузка видео…"
                videoUploaded -> "Видео загружено на сервер"
                else -> "Выберите короткое видео с телефона — так вас увидят в ленте"
            },
            fontSize = scaleSp(13f),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        if (uploadError != null) {
            Spacer(Modifier.height(scaleDp(6f)))
            Text(
                text = uploadError!!,
                fontSize = scaleSp(12f),
                color = MaterialTheme.colorScheme.error,
            )
        }
        Spacer(Modifier.height(scaleDp(10f)))
        OutlinedButton(
            onClick = { launchVideoPicker(videoPicker) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uploading && !videoUploaded,
            colors = AppButtonDefaults.outlinedBlue(),
        ) {
            if (uploading) {
                CircularProgressIndicator(
                    modifier = Modifier.height(scaleDp(18f)),
                    strokeWidth = scaleDp(2f),
                )
            } else {
                Text(
                    text = if (videoUploaded) "Видео загружено" else "Выбрать видео с телефона",
                    fontSize = scaleSp(14f)
                )
            }
        }

        Spacer(Modifier.height(scaleDp(24f)))
        Button(
            onClick = onComplete,
            modifier = Modifier.fillMaxWidth(),
            enabled = videoUploaded && !uploading,
            colors = AppButtonDefaults.blue(),
        ) {
            Text("Готово", fontSize = scaleSp(14f))
        }
        Spacer(Modifier.height(scaleDp(10f)))
        OutlinedButton(
            onClick = onSkip,
            modifier = Modifier.fillMaxWidth(),
            colors = AppButtonDefaults.outlinedRed(),
        ) {
            Text("Пропустить", fontSize = scaleSp(14f))
        }
    }
}
