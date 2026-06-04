package com.example.androiddatingapp.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.example.androiddatingapp.ui.model.Gender
import com.example.androiddatingapp.ui.model.UserPreferences
import com.example.androiddatingapp.ui.theme.AppButtonDefaults
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PreferencesSheet(
    initial: UserPreferences,
    cityName: String,
    onDismiss: () -> Unit,
    onSave: (UserPreferences) -> Unit,
    scaleDp: (Float) -> Dp,
    scaleSp: (Float) -> TextUnit,
    showSkip: Boolean = false,
    onSkip: (() -> Unit)? = null,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var maleSelected by remember(initial) {
        mutableStateOf(Gender.MALE in initial.preferredGenders)
    }
    var femaleSelected by remember(initial) {
        mutableStateOf(Gender.FEMALE in initial.preferredGenders)
    }
    var radiusKm by remember(initial.radiusKm) {
        mutableIntStateOf(
            initial.radiusKm.coerceIn(UserPreferences.MIN_RADIUS_KM, UserPreferences.MAX_RADIUS_KM),
        )
    }
    var ageRange by remember(initial.minAge, initial.maxAge) {
        mutableStateOf(initial.minAge.toFloat()..initial.maxAge.toFloat())
    }
    var validationError by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = scaleDp(16f), vertical = scaleDp(10f)),
        ) {
            Text(
                text = "Предпочтения",
                fontSize = scaleSp(20f),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(scaleDp(6f)))
            Text(
                text = "Город: $cityName",
                fontSize = scaleSp(13f),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
            Spacer(Modifier.height(scaleDp(16f)))

            Text(
                text = "Кого показывать в ленте",
                fontSize = scaleSp(15f),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(scaleDp(8f)))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(scaleDp(8f))) {
                FilterChip(
                    selected = maleSelected,
                    onClick = { maleSelected = !maleSelected },
                    label = { Text(Gender.MALE.labelRu, fontSize = scaleSp(13f)) },
                )
                FilterChip(
                    selected = femaleSelected,
                    onClick = { femaleSelected = !femaleSelected },
                    label = { Text(Gender.FEMALE.labelRu, fontSize = scaleSp(13f)) },
                )
            }

            Spacer(Modifier.height(scaleDp(18f)))
            Text(
                text = "Расстояние от города",
                fontSize = scaleSp(15f),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(scaleDp(4f)))
            Text(
                text = "1–${UserPreferences.MAX_RADIUS_KM} км",
                fontSize = scaleSp(12f),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            )
            Spacer(Modifier.height(scaleDp(10f)))
            RadiusKmStepper(
                valueKm = radiusKm,
                onValueChange = { radiusKm = it },
                scaleDp = scaleDp,
                scaleSp = scaleSp,
            )

            Spacer(Modifier.height(scaleDp(12f)))
            Text(
                text = "Возраст: ${ageRange.start.roundToInt()}–${ageRange.endInclusive.roundToInt()} лет",
                fontSize = scaleSp(15f),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            RangeSlider(
                value = ageRange,
                onValueChange = { ageRange = it },
                valueRange = UserPreferences.MIN_AGE.toFloat()..UserPreferences.MAX_AGE.toFloat(),
                steps = UserPreferences.MAX_AGE - UserPreferences.MIN_AGE - 1,
            )

            if (validationError != null) {
                Spacer(Modifier.height(scaleDp(8f)))
                Text(
                    text = validationError!!,
                    fontSize = scaleSp(12f),
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Spacer(Modifier.height(scaleDp(16f)))
            Button(
                onClick = {
                    val genders = buildSet {
                        if (maleSelected) add(Gender.MALE)
                        if (femaleSelected) add(Gender.FEMALE)
                    }
                    if (genders.isEmpty()) {
                        validationError = "Выберите хотя бы один пол"
                        return@Button
                    }
                    validationError = null
                    val prefs = UserPreferences(
                        preferredGenders = genders,
                        minAge = ageRange.start.roundToInt(),
                        maxAge = ageRange.endInclusive.roundToInt(),
                        radiusKm = radiusKm.coerceIn(
                            UserPreferences.MIN_RADIUS_KM,
                            UserPreferences.MAX_RADIUS_KM,
                        ),
                    )
                    onSave(prefs)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = AppButtonDefaults.blue(),
            ) {
                Text("Сохранить", fontSize = scaleSp(14f))
            }

            if (showSkip && onSkip != null) {
                Spacer(Modifier.height(scaleDp(10f)))
                OutlinedButton(
                    onClick = onSkip,
                    modifier = Modifier.fillMaxWidth(),
                    colors = AppButtonDefaults.outlinedBlue(),
                ) {
                    Text("Пропустить (по умолчанию)", fontSize = scaleSp(13f))
                }
            }

            Spacer(Modifier.height(scaleDp(20f)))
        }
    }
}

@Composable
private fun RadiusKmStepper(
    valueKm: Int,
    onValueChange: (Int) -> Unit,
    scaleDp: (Float) -> Dp,
    scaleSp: (Float) -> TextUnit,
    modifier: Modifier = Modifier,
) {
    fun adjust(delta: Int) {
        onValueChange(
            (valueKm + delta).coerceIn(UserPreferences.MIN_RADIUS_KM, UserPreferences.MAX_RADIUS_KM),
        )
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(scaleDp(6f))) {
            RadiusStepButton(label = "−100", onClick = { adjust(-100) }, scaleDp = scaleDp, scaleSp = scaleSp)
            RadiusStepButton(label = "−10", onClick = { adjust(-10) }, scaleDp = scaleDp, scaleSp = scaleSp)
        }

        Text(
            text = "$valueKm км",
            fontSize = scaleSp(18f),
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = scaleDp(8f)),
        )

        Row(horizontalArrangement = Arrangement.spacedBy(scaleDp(6f))) {
            RadiusStepButton(label = "+10", onClick = { adjust(+10) }, scaleDp = scaleDp, scaleSp = scaleSp)
            RadiusStepButton(label = "+100", onClick = { adjust(+100) }, scaleDp = scaleDp, scaleSp = scaleSp)
        }
    }
}

@Composable
private fun RadiusStepButton(
    label: String,
    onClick: () -> Unit,
    scaleDp: (Float) -> Dp,
    scaleSp: (Float) -> TextUnit,
) {
    OutlinedButton(
        onClick = onClick,
        colors = AppButtonDefaults.outlinedBlue(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = scaleDp(8f),
            vertical = scaleDp(6f),
        ),
    ) {
        Text(text = label, fontSize = scaleSp(12f))
    }
}
