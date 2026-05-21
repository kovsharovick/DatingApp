package com.example.androiddatingapp.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit

private const val DESCRIPTION_MAX_LENGTH = 500

@Composable
fun OnboardingScreen(
    userName: String,
    onComplete: (description: String, videoUploaded: Boolean) -> Unit,
    onSkip: () -> Unit,
    scaleDp: (Float) -> Dp,
    scaleSp: (Float) -> TextUnit,
    modifier: Modifier = Modifier,
) {
    var description by remember { mutableStateOf("") }
    var videoUploaded by remember { mutableStateOf(false) }

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
            text = "Расскажите о себе и добавьте видео для анкеты. " +
                "Без видео лента и сообщения будут недоступны.",
            fontSize = scaleSp(14f),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
        )
        Spacer(Modifier.height(scaleDp(20f)))

        OutlinedTextField(
            value = description,
            onValueChange = { if (it.length <= DESCRIPTION_MAX_LENGTH) description = it },
            label = { Text("О себе", fontSize = scaleSp(12f)) },
            placeholder = { Text("Необязательно", fontSize = scaleSp(12f)) },
            supportingText = {
                Text(
                    text = "${description.length}/$DESCRIPTION_MAX_LENGTH",
                    fontSize = scaleSp(11f)
                )
            },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 6
        )
        Spacer(Modifier.height(scaleDp(16f)))

        Text(
            text = "Видео анкеты",
            fontSize = scaleSp(16f),
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(scaleDp(8f)))
        Text(
            text = if (videoUploaded) {
                "Видео добавлено (заглушка)"
            } else {
                "Загрузите короткое видео — так вас увидят в ленте"
            },
            fontSize = scaleSp(13f),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        Spacer(Modifier.height(scaleDp(10f)))
        OutlinedButton(
            onClick = { videoUploaded = true },
            modifier = Modifier.fillMaxWidth(),
            enabled = !videoUploaded
        ) {
            Text(
                text = if (videoUploaded) "Видео загружено" else "Загрузить видео",
                fontSize = scaleSp(14f)
            )
        }

        Spacer(Modifier.height(scaleDp(24f)))
        Button(
            onClick = { onComplete(description.trim(), videoUploaded) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Готово", fontSize = scaleSp(14f))
        }
        Spacer(Modifier.height(scaleDp(10f)))
        OutlinedButton(
            onClick = onSkip,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Пропустить", fontSize = scaleSp(14f))
        }
    }
}
