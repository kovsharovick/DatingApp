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
import com.example.androiddatingapp.ui.model.Gender
import com.example.androiddatingapp.ui.model.UserAccount
import com.example.androiddatingapp.ui.model.UserPreferences
import com.example.androiddatingapp.ui.model.toPreferences
import com.example.androiddatingapp.ui.profile.PreferencesSheet
import com.example.androiddatingapp.ui.theme.AppButtonDefaults
import kotlinx.coroutines.launch

private enum class OnboardingStep { VIDEO, PREFERENCES }

@Composable
fun OnboardingScreen(
    account: UserAccount,
    hasVideoAlready: Boolean,
    onUploadVideo: suspend (Uri) -> Result<Unit>,
    onSavePreferences: suspend (UserPreferences) -> Result<Unit>,
    onSkipPreferences: suspend () -> Result<Unit>,
    onFinished: () -> Unit,
    scaleDp: (Float) -> Dp,
    scaleSp: (Float) -> TextUnit,
    modifier: Modifier = Modifier,
) {
    var step by remember {
        mutableStateOf(if (hasVideoAlready) OnboardingStep.PREFERENCES else OnboardingStep.VIDEO)
    }
    var videoUploaded by remember { mutableStateOf(hasVideoAlready) }
    var uploading by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }
    var prefsSaving by remember { mutableStateOf(false) }
    var prefsError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val videoPicker = rememberVideoPicker { uri ->
        scope.launch {
            uploading = true
            uploadError = null
            onUploadVideo(uri)
                .onSuccess {
                    videoUploaded = true
                    step = OnboardingStep.PREFERENCES
                }
                .onFailure { uploadError = it.message }
            uploading = false
        }
    }

    when (step) {
        OnboardingStep.VIDEO -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = scaleDp(20f), vertical = scaleDp(24f)),
            ) {
                Text(
                    text = "Привет, ${account.name}!",
                    fontSize = scaleSp(24f),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(scaleDp(8f)))
                Text(
                    text = "Добавьте видео для анкеты. Без видео лента будет недоступна. " +
                        "После загрузки настроите предпочтения для ленты.",
                    fontSize = scaleSp(14f),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                )
                Spacer(Modifier.height(scaleDp(20f)))

                Text(
                    text = "Видео анкеты",
                    fontSize = scaleSp(16f),
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(scaleDp(8f)))
                Text(
                    text = when {
                        uploading -> "Загрузка видео…"
                        videoUploaded -> "Видео загружено — переходим к предпочтениям"
                        else -> "Выберите короткое видео с телефона"
                    },
                    fontSize = scaleSp(13f),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
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
                            fontSize = scaleSp(14f),
                        )
                    }
                }

                if (videoUploaded) {
                    Spacer(Modifier.height(scaleDp(16f)))
                    Button(
                        onClick = { step = OnboardingStep.PREFERENCES },
                        modifier = Modifier.fillMaxWidth(),
                        colors = AppButtonDefaults.blue(),
                    ) {
                        Text("Далее: предпочтения", fontSize = scaleSp(14f))
                    }
                }
            }
        }

        OnboardingStep.PREFERENCES -> {
            val initialPrefs = remember(account) {
                if (account.preferencesConfigured) {
                    account.toPreferences()
                } else {
                    UserPreferences(
                        preferredGenders = setOf(
                            UserPreferences.oppositeGender(account.gender),
                        ),
                        minAge = (account.ageYears() ?: 25).coerceIn(
                            UserPreferences.MIN_AGE,
                            UserPreferences.MAX_AGE,
                        ),
                        maxAge = (account.ageYears() ?: 25).coerceIn(
                            UserPreferences.MIN_AGE,
                            UserPreferences.MAX_AGE,
                        ),
                        radiusKm = UserPreferences.DEFAULT_RADIUS_KM,
                    )
                }
            }

            if (prefsError != null) {
                Column(
                    modifier = modifier.padding(scaleDp(16f)),
                ) {
                    Text(
                        text = prefsError!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = scaleSp(13f),
                    )
                }
            }

            PreferencesSheet(
                initial = initialPrefs,
                cityName = account.city,
                onDismiss = {
                    scope.launch {
                        onSkipPreferences()
                            .onSuccess { onFinished() }
                            .onFailure { prefsError = it.message }
                    }
                },
                onSave = { prefs ->
                    scope.launch {
                        prefsSaving = true
                        prefsError = null
                        onSavePreferences(prefs)
                            .onSuccess {
                                prefsSaving = false
                                onFinished()
                            }
                            .onFailure {
                                prefsError = it.message
                                prefsSaving = false
                            }
                    }
                },
                showSkip = true,
                onSkip = {
                    scope.launch {
                        prefsSaving = true
                        prefsError = null
                        onSkipPreferences()
                            .onSuccess {
                                prefsSaving = false
                                onFinished()
                            }
                            .onFailure {
                                prefsError = it.message
                                prefsSaving = false
                            }
                    }
                },
                scaleDp = scaleDp,
                scaleSp = scaleSp,
            )
        }
    }
}
