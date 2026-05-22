package com.example.androiddatingapp.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.example.androiddatingapp.ui.components.DateOfBirthTextField
import com.example.androiddatingapp.ui.components.ExpandableDescription
import com.example.androiddatingapp.ui.components.GlowingSubscriptionButton
import com.example.androiddatingapp.ui.components.PasswordTextField
import com.example.androiddatingapp.ui.model.Gender
import com.example.androiddatingapp.ui.model.UserAccount
import com.example.androiddatingapp.ui.theme.AppBlue
import com.example.androiddatingapp.ui.theme.AppBlueLight
import com.example.androiddatingapp.ui.theme.AppButtonDefaults
import com.example.androiddatingapp.ui.theme.AppRed
import com.example.androiddatingapp.ui.theme.AppRedLight
import com.example.androiddatingapp.ui.theme.DarkBackground
import com.example.androiddatingapp.ui.theme.DarkCard
import com.example.androiddatingapp.ui.util.DateOfBirthInput
import com.example.androiddatingapp.ui.util.formatAgeLabel

private const val DESCRIPTION_MAX_LENGTH = 500

private fun profileCityAgeLine(city: String, ageYears: Int?): String =
    when (val age = ageYears) {
        null -> city
        else -> "$city, ${formatAgeLabel(age)}"
    }

private fun UserAccount.toProfileUi(): UserProfileUi = UserProfileUi(
    name = name,
    dateOfBirth = dateOfBirth,
    city = city,
    description = description,
    gender = gender,
    videoTitle = when {
        videoTitle.isNotBlank() -> videoTitle
        hasVideo -> "video_profile.mp4"
        else -> ""
    },
)

@Composable
fun ProfileScreen(
    account: UserAccount,
    onAccountUpdate: (UserAccount) -> Unit,
    openSettings: Boolean,
    onOpenSettingsConsumed: () -> Unit,
    openSubscription: Boolean = false,
    onOpenSubscriptionConsumed: () -> Unit = {},
    onLogout: () -> Unit,
    scaleDp: (Float) -> Dp,
    scaleSp: (Float) -> TextUnit,
    modifier: Modifier = Modifier
) {
    val profile = account.toProfileUi()

    var editSheetOpen by remember { mutableStateOf(false) }
    var settingsSheetOpen by remember { mutableStateOf(false) }
    var changeVideoSheetOpen by remember { mutableStateOf(false) }
    var changePasswordOpen by remember { mutableStateOf(false) }
    var subscriptionSheetOpen by remember { mutableStateOf(false) }
    var showPreview by remember { mutableStateOf(false) }
    var videoVersion by remember { mutableIntStateOf(1) }

    if (openSettings) {
        settingsSheetOpen = true
        onOpenSettingsConsumed()
    }
    if (openSubscription) {
        subscriptionSheetOpen = true
        onOpenSubscriptionConsumed()
    }

    Surface(modifier.fillMaxSize()) {
        if (showPreview) {
            ProfilePreviewScreen(
                profile = profile,
                onBack = { showPreview = false },
                onChangeVideo = { changeVideoSheetOpen = true },
                scaleDp = scaleDp,
                scaleSp = scaleSp,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            ProfileMainScreen(
                profile = profile,
                cityAgeLine = profileCityAgeLine(account.city, account.ageYears()),
                hasVideo = account.hasVideo,
                remainingSwipes = account.remainingSwipes(),
                onOpenSubscription = { subscriptionSheetOpen = true },
                onOpenPreview = { showPreview = true },
                onUploadVideo = { changeVideoSheetOpen = true },
                onOpenEdit = { editSheetOpen = true },
                onOpenSettings = { settingsSheetOpen = true },
                scaleDp = scaleDp,
                scaleSp = scaleSp,
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    if (editSheetOpen) {
        EditProfileSheet(
            initial = profile,
            onDismiss = { editSheetOpen = false },
            onSave = { updated ->
                onAccountUpdate(
                    account.copy(
                        name = updated.name,
                        dateOfBirth = updated.dateOfBirth,
                        city = updated.city,
                        description = updated.description.take(DESCRIPTION_MAX_LENGTH),
                    )
                )
                editSheetOpen = false
            },
            scaleDp = scaleDp,
            scaleSp = scaleSp
        )
    }

    if (settingsSheetOpen) {
        SettingsSheet(
            onDismiss = { settingsSheetOpen = false },
            isProfileActive = account.isProfileActive,
            onToggleProfileActive = { active ->
                onAccountUpdate(account.copy(isProfileActive = active))
            },
            onChangePassword = { changePasswordOpen = true },
            onLogout = onLogout,
            scaleDp = scaleDp,
            scaleSp = scaleSp
        )
    }

    if (changeVideoSheetOpen) {
        ChangeVideoSheet(
            currentVideoTitle = profile.videoTitle,
            hasVideo = account.hasVideo,
            onDismiss = { changeVideoSheetOpen = false },
            onUploadNewVideo = {
                videoVersion += 1
                onAccountUpdate(
                    account.copy(
                        hasVideo = true,
                        videoTitle = "video_profile_v$videoVersion.mp4",
                    )
                )
                changeVideoSheetOpen = false
            },
            onDeleteVideo = {
                onAccountUpdate(account.copy(hasVideo = false, videoTitle = ""))
                changeVideoSheetOpen = false
                showPreview = false
            },
            scaleDp = scaleDp,
            scaleSp = scaleSp
        )
    }

    if (changePasswordOpen) {
        ChangePasswordSheet(
            onDismiss = { changePasswordOpen = false },
            scaleDp = scaleDp,
            scaleSp = scaleSp
        )
    }

    if (subscriptionSheetOpen) {
        SwipeSubscriptionSheet(
            remainingSwipes = account.remainingSwipes(),
            onDismiss = { subscriptionSheetOpen = false },
            onPurchase = { packSize ->
                onAccountUpdate(account.withBonusSwipesPurchased(packSize))
                subscriptionSheetOpen = false
            },
            scaleDp = scaleDp,
            scaleSp = scaleSp,
        )
    }
}

private data class UserProfileUi(
    val name: String,
    val dateOfBirth: String,
    val city: String,
    val description: String,
    val gender: Gender,
    val videoTitle: String,
)

@Composable
private fun ProfileMainScreen(
    profile: UserProfileUi,
    cityAgeLine: String,
    hasVideo: Boolean,
    remainingSwipes: Int,
    onOpenSubscription: () -> Unit,
    onOpenPreview: () -> Unit,
    onUploadVideo: () -> Unit,
    onOpenEdit: () -> Unit,
    onOpenSettings: () -> Unit,
    scaleDp: (Float) -> Dp,
    scaleSp: (Float) -> TextUnit,
    modifier: Modifier = Modifier
) {
    val scroll = rememberScrollState()

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scroll)
            .padding(horizontal = scaleDp(14f), vertical = scaleDp(12f))
    ) {
        Text(
            text = "Профиль",
            fontSize = scaleSp(20f),
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(scaleDp(12f)))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(scaleDp(18f)))
                .background(MaterialTheme.colorScheme.surface)
                .padding(scaleDp(14f)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GenderAvatar(
                name = profile.name,
                gender = profile.gender,
                size = scaleDp(68f),
                scaleSp = scaleSp
            )
            Spacer(Modifier.width(scaleDp(12f)))
            Column(Modifier.weight(1f)) {
                Text(
                    text = profile.name,
                    fontSize = scaleSp(18f),
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(scaleDp(4f)))
                Text(
                    text = cityAgeLine,
                    fontSize = scaleSp(13f),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                OutlinedButton(
                    onClick = onOpenEdit,
                    colors = AppButtonDefaults.outlinedBlue(),
                ) {
                    Text(text = "Редакт.", fontSize = scaleSp(13f))
                }
                Spacer(Modifier.height(scaleDp(8f)))
                OutlinedButton(
                    onClick = onOpenSettings,
                    colors = AppButtonDefaults.outlinedBlue(),
                ) {
                    Text(text = "Настр.", fontSize = scaleSp(13f))
                }
            }
        }

        Spacer(Modifier.height(scaleDp(12f)))

        GlowingSubscriptionButton(
            remainingSwipes = remainingSwipes,
            onClick = onOpenSubscription,
            scaleDp = scaleDp,
            scaleSp = scaleSp,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(scaleDp(12f)))

        DescriptionCard(
            description = profile.description,
            scaleDp = scaleDp,
            scaleSp = scaleSp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(scaleDp(12f)))

        VideoCard(
            title = "Видео анкета",
            subtitle = if (hasVideo) profile.videoTitle else "Видео не загружено",
            hasVideo = hasVideo,
            scaleDp = scaleDp,
            scaleSp = scaleSp,
            onPreview = onOpenPreview,
            onUploadVideo = onUploadVideo,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(scaleDp(18f)))
    }
}

@Composable
private fun ProfilePreviewScreen(
    profile: UserProfileUi,
    onBack: () -> Unit,
    onChangeVideo: () -> Unit,
    scaleDp: (Float) -> Dp,
    scaleSp: (Float) -> TextUnit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(DarkBackground)
            .padding(scaleDp(12f))
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "←",
                fontSize = scaleSp(20f),
                modifier = Modifier
                    .clip(RoundedCornerShape(scaleDp(10f)))
                    .clickable { onBack() }
                    .padding(horizontal = scaleDp(10f), vertical = scaleDp(6f)),
                color = Color.White
            )
            Spacer(Modifier.width(scaleDp(10f)))
            Text(
                text = "Просмотр анкеты",
                fontSize = scaleSp(18f),
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }

        Spacer(Modifier.height(scaleDp(12f)))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(scaleDp(18f)))
                .background(DarkCard),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Видео: ${profile.videoTitle}",
                fontSize = scaleSp(14f),
                color = Color.White.copy(alpha = 0.9f)
            )
        }

        Spacer(Modifier.height(scaleDp(12f)))

        Button(
            onClick = onChangeVideo,
            modifier = Modifier.fillMaxWidth(),
            colors = AppButtonDefaults.blue()
        ) {
            Text(text = "Изменить видео", fontSize = scaleSp(14f))
        }
    }
}

@Composable
private fun DescriptionCard(
    description: String,
    scaleDp: (Float) -> Dp,
    scaleSp: (Float) -> TextUnit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(scaleDp(18f)))
            .background(MaterialTheme.colorScheme.surface)
            .padding(scaleDp(14f))
    ) {
        Text(
            text = "О себе",
            fontSize = scaleSp(16f),
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(scaleDp(8f)))
        if (description.isBlank()) {
            Text(
                text = "Добавьте описание в разделе «Редакт.»",
                fontSize = scaleSp(13f),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        } else {
            ExpandableDescription(
                text = description,
                fontSize = scaleSp(13f),
                textColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                linkColor = AppBlueLight,
                collapsedMaxLines = 1,
            )
        }
    }
}

@Composable
private fun GenderAvatar(
    name: String,
    gender: Gender,
    size: Dp,
    scaleSp: (Float) -> TextUnit,
    modifier: Modifier = Modifier
) {
    val (bg, letterColor) = when (gender) {
        Gender.MALE -> AppBlueLight.copy(alpha = 0.35f) to AppBlue
        Gender.FEMALE -> AppRedLight.copy(alpha = 0.35f) to AppRed
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.trim().take(1).uppercase(),
            fontSize = scaleSp(22f),
            fontWeight = FontWeight.Bold,
            color = letterColor
        )
    }
}

@Composable
private fun VideoCard(
    title: String,
    subtitle: String,
    hasVideo: Boolean,
    onPreview: () -> Unit,
    onUploadVideo: () -> Unit,
    scaleDp: (Float) -> Dp,
    scaleSp: (Float) -> TextUnit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(scaleDp(18f)))
            .background(MaterialTheme.colorScheme.surface)
            .padding(scaleDp(14f))
    ) {
        Text(
            text = title,
            fontSize = scaleSp(16f),
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(scaleDp(6f)))

        Text(
            text = subtitle,
            fontSize = scaleSp(12f),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.height(scaleDp(12f)))

        if (hasVideo) {
            Button(
                onClick = onPreview,
                modifier = Modifier.fillMaxWidth(),
                colors = AppButtonDefaults.blue()
            ) {
                Text(text = "Просмотр", fontSize = scaleSp(13f))
            }
        } else {
            Button(
                onClick = onUploadVideo,
                modifier = Modifier.fillMaxWidth(),
                colors = AppButtonDefaults.blue()
            ) {
                Text(text = "Загрузить видео", fontSize = scaleSp(13f))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileSheet(
    initial: UserProfileUi,
    onDismiss: () -> Unit,
    onSave: (UserProfileUi) -> Unit,
    scaleDp: (Float) -> Dp,
    scaleSp: (Float) -> TextUnit,
) {
    var name by remember(initial.name) { mutableStateOf(initial.name) }
    var dateOfBirth by remember(initial.dateOfBirth) { mutableStateOf(initial.dateOfBirth) }
    var city by remember(initial.city) { mutableStateOf(initial.city) }
    var description by remember(initial.description) { mutableStateOf(initial.description) }
    var dateOfBirthError by remember { mutableStateOf<String?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = scaleDp(14f), vertical = scaleDp(10f))
        ) {
            Text(
                text = "Редактирование профиля",
                fontSize = scaleSp(16f),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(scaleDp(12f)))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Имя", fontSize = scaleSp(12f)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(scaleDp(10f)))
            DateOfBirthTextField(
                value = dateOfBirth,
                onValueChange = {
                    dateOfBirth = it
                    dateOfBirthError = null
                },
                label = "Дата рождения",
                scaleSp = scaleSp,
            )
            dateOfBirthError?.let { err ->
                Spacer(Modifier.height(scaleDp(4f)))
                Text(
                    text = err,
                    fontSize = scaleSp(11f),
                    color = AppRedLight
                )
            }
            Spacer(Modifier.height(scaleDp(10f)))
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("Город", fontSize = scaleSp(12f)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(scaleDp(10f)))
            OutlinedTextField(
                value = description,
                onValueChange = { if (it.length <= DESCRIPTION_MAX_LENGTH) description = it },
                label = { Text("О себе", fontSize = scaleSp(12f)) },
                placeholder = { Text("Расскажите о себе", fontSize = scaleSp(12f)) },
                supportingText = {
                    Text(
                        text = "${description.length}/$DESCRIPTION_MAX_LENGTH",
                        fontSize = scaleSp(11f),
                        color = if (description.length >= DESCRIPTION_MAX_LENGTH) {
                            AppRedLight
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6
            )

            Spacer(Modifier.height(scaleDp(14f)))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(scaleDp(10f))
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = AppButtonDefaults.outlinedRed(),
                ) {
                    Text("Отмена", fontSize = scaleSp(13f))
                }
                Button(
                    onClick = {
                        val dob = dateOfBirth.trim()
                        val err = DateOfBirthInput.validateDateOfBirth(dob)
                        if (err != null) {
                            dateOfBirthError = err
                            return@Button
                        }
                        onSave(
                            initial.copy(
                                name = name.trim().ifBlank { initial.name },
                                dateOfBirth = dob,
                                city = city.trim().ifBlank { initial.city },
                                description = description.trim().take(DESCRIPTION_MAX_LENGTH),
                            )
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = AppButtonDefaults.blue(),
                ) {
                    Text("Сохранить", fontSize = scaleSp(13f))
                }
            }

            Spacer(Modifier.height(scaleDp(18f)))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsSheet(
    onDismiss: () -> Unit,
    isProfileActive: Boolean,
    onToggleProfileActive: (Boolean) -> Unit,
    onChangePassword: () -> Unit,
    onLogout: () -> Unit,
    scaleDp: (Float) -> Dp,
    scaleSp: (Float) -> TextUnit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = scaleDp(14f), vertical = scaleDp(10f))
        ) {
            Text(
                text = "Настройки",
                fontSize = scaleSp(16f),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(scaleDp(12f)))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(scaleDp(14f)))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                    .padding(scaleDp(12f)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = if (isProfileActive) "Анкета активна" else "Анкета на паузе",
                        fontSize = scaleSp(14f),
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(scaleDp(4f)))
                    Text(
                        text = if (isProfileActive) {
                            "Другие видят твою анкету в ленте. Сообщения доступны."
                        } else {
                            "Твою анкету не показываем в ленте. Сообщения по-прежнему доступны."
                        },
                        fontSize = scaleSp(12f),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                    )
                }
                Switch(
                    checked = isProfileActive,
                    onCheckedChange = { onToggleProfileActive(it) }
                )
            }

            Spacer(Modifier.height(scaleDp(14f)))

            Spacer(Modifier.height(scaleDp(16f)))

            Text(
                text = "Аккаунт",
                fontSize = scaleSp(14f),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(scaleDp(10f)))

            Button(
                onClick = onChangePassword,
                colors = AppButtonDefaults.blue(),
            ) {
                Text(text = "Поменять пароль", fontSize = scaleSp(13f))
            }

            Spacer(Modifier.height(scaleDp(10f)))

            Button(
                onClick = onLogout,
                colors = AppButtonDefaults.red(),
            ) {
                Text(text = "Выйти из аккаунта", fontSize = scaleSp(13f))
            }

            Spacer(Modifier.height(scaleDp(18f)))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChangeVideoSheet(
    currentVideoTitle: String,
    hasVideo: Boolean,
    onDismiss: () -> Unit,
    onUploadNewVideo: () -> Unit,
    onDeleteVideo: () -> Unit,
    scaleDp: (Float) -> Dp,
    scaleSp: (Float) -> TextUnit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = scaleDp(14f), vertical = scaleDp(10f))
        ) {
            Text(
                text = "Изменить видео анкеты",
                fontSize = scaleSp(16f),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(scaleDp(8f)))
            Text(
                text = "Текущее: $currentVideoTitle",
                fontSize = scaleSp(12f),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(scaleDp(14f)))

            Button(
                onClick = onUploadNewVideo,
                modifier = Modifier.fillMaxWidth(),
                colors = AppButtonDefaults.blue(),
            ) {
                Text(
                    text = if (hasVideo) "Загрузить новое видео" else "Загрузить видео",
                    fontSize = scaleSp(13f)
                )
            }

            if (hasVideo) {
                Spacer(Modifier.height(scaleDp(10f)))
                OutlinedButton(
                    onClick = onDeleteVideo,
                    modifier = Modifier.fillMaxWidth(),
                    colors = AppButtonDefaults.outlinedRed(),
                ) {
                    Text(text = "Удалить видео", fontSize = scaleSp(13f))
                }
            }

            Spacer(Modifier.height(scaleDp(18f)))
        }
    }
}

private fun swipePackPrice(swipes: Int): String = when (swipes) {
    10 -> "149 ₽"
    20 -> "249 ₽"
    50 -> "499 ₽"
    else -> ""
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeSubscriptionSheet(
    remainingSwipes: Int,
    onDismiss: () -> Unit,
    onPurchase: (Int) -> Unit,
    scaleDp: (Float) -> Dp,
    scaleSp: (Float) -> TextUnit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = scaleDp(14f), vertical = scaleDp(10f)),
        ) {
            Text(
                text = "Дополнительные свайпы",
                fontSize = scaleSp(16f),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(scaleDp(6f)))
            Text(
                text = "Каждый день — ${UserAccount.DAILY_FREE_SWIPES} бесплатных свайпов. " +
                    "Сейчас доступно: $remainingSwipes.",
                fontSize = scaleSp(12f),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
            )
            Spacer(Modifier.height(scaleDp(14f)))

            UserAccount.SWIPE_PACK_OPTIONS.forEach { pack ->
                val price = swipePackPrice(pack)
                Button(
                    onClick = { onPurchase(pack) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = AppButtonDefaults.red(),
                ) {
                    Text(
                        text = "+$pack свайпов · $price",
                        fontSize = scaleSp(14f),
                    )
                }
                Spacer(Modifier.height(scaleDp(10f)))
            }

            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = AppButtonDefaults.outlinedBlue(),
            ) {
                Text(text = "Закрыть", fontSize = scaleSp(13f))
            }

            Spacer(Modifier.height(scaleDp(18f)))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChangePasswordSheet(
    onDismiss: () -> Unit,
    scaleDp: (Float) -> Dp,
    scaleSp: (Float) -> TextUnit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    val canSave = newPass.isNotBlank() && newPass == confirm

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = scaleDp(14f), vertical = scaleDp(10f))
        ) {
            Text(
                text = "Смена пароля",
                fontSize = scaleSp(16f),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(scaleDp(12f)))

            PasswordTextField(
                value = oldPass,
                onValueChange = { oldPass = it },
                label = "Текущий пароль",
                scaleSp = scaleSp,
            )
            Spacer(Modifier.height(scaleDp(10f)))
            PasswordTextField(
                value = newPass,
                onValueChange = { newPass = it },
                label = "Новый пароль",
                scaleSp = scaleSp,
            )
            Spacer(Modifier.height(scaleDp(10f)))
            PasswordTextField(
                value = confirm,
                onValueChange = { confirm = it },
                label = "Повторите новый пароль",
                scaleSp = scaleSp,
            )

            Spacer(Modifier.height(scaleDp(14f)))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(scaleDp(10f))
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = AppButtonDefaults.outlinedRed(),
                ) {
                    Text("Отмена", fontSize = scaleSp(13f))
                }
                Button(
                    onClick = { onDismiss() },
                    enabled = canSave,
                    modifier = Modifier.weight(1f),
                    colors = AppButtonDefaults.blue(),
                ) {
                    Text("Сохранить", fontSize = scaleSp(13f))
                }
            }

            Spacer(Modifier.height(scaleDp(18f)))
        }
    }
}

