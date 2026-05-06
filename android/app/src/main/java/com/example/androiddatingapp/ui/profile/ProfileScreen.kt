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

@Composable
fun ProfileScreen(
    isProfileActive: Boolean,
    openSettings: Boolean,
    onOpenSettingsConsumed: () -> Unit,
    onToggleProfileActive: (Boolean) -> Unit,
    onLogout: () -> Unit,
    scaleDp: (Float) -> Dp,
    scaleSp: (Float) -> TextUnit,
    modifier: Modifier = Modifier
) {
    var profile by remember {
        mutableStateOf(
            UserProfileUi(
                name = "Анастасия",
                age = "23",
                city = "Москва",
                avatarSeed = 1,
                videoTitle = "video_profile_v1.mp4"
            )
        )
    }

    var editSheetOpen by remember { mutableStateOf(false) }
    var settingsSheetOpen by remember { mutableStateOf(false) }
    var changeVideoSheetOpen by remember { mutableStateOf(false) }
    var changePasswordOpen by remember { mutableStateOf(false) }
    var showPreview by remember { mutableStateOf(false) }
    var videoVersion by remember { mutableIntStateOf(1) }

    if (openSettings) {
        settingsSheetOpen = true
        onOpenSettingsConsumed()
    }

    Surface(modifier.fillMaxSize()) {
        if (showPreview) {
            ProfilePreviewScreen(
                profile = profile,
                onBack = { showPreview = false },
                scaleDp = scaleDp,
                scaleSp = scaleSp,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            ProfileMainScreen(
                profile = profile,
                onOpenPreview = { showPreview = true },
                onOpenEdit = { editSheetOpen = true },
                onOpenSettings = { settingsSheetOpen = true },
                onOpenChangeVideo = { changeVideoSheetOpen = true },
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
                profile = updated
                editSheetOpen = false
            },
            scaleDp = scaleDp,
            scaleSp = scaleSp
        )
    }

    if (settingsSheetOpen) {
        SettingsSheet(
            profile = profile,
            onDismiss = { settingsSheetOpen = false },
            isProfileActive = isProfileActive,
            onToggleProfileActive = onToggleProfileActive,
            onChangePassword = { changePasswordOpen = true },
            onLogout = onLogout,
            scaleDp = scaleDp,
            scaleSp = scaleSp
        )
    }

    if (changeVideoSheetOpen) {
        ChangeVideoSheet(
            currentVideoTitle = profile.videoTitle,
            onDismiss = { changeVideoSheetOpen = false },
            onUploadNewVideo = {
                videoVersion += 1
                profile = profile.copy(videoTitle = "video_profile_v$videoVersion.mp4")
                changeVideoSheetOpen = false
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
}

private data class UserProfileUi(
    val name: String,
    val age: String,
    val city: String,
    val avatarSeed: Int,
    val videoTitle: String
)

@Composable
private fun ProfileMainScreen(
    profile: UserProfileUi,
    onOpenPreview: () -> Unit,
    onOpenEdit: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenChangeVideo: () -> Unit,
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
            Avatar(
                name = profile.name,
                size = scaleDp(68f),
                seed = profile.avatarSeed,
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
                    text = "${profile.age} • ${profile.city}",
                    fontSize = scaleSp(13f),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                OutlinedButton(onClick = onOpenEdit) {
                    Text(text = "Редакт.", fontSize = scaleSp(13f))
                }
                Spacer(Modifier.height(scaleDp(8f)))
                OutlinedButton(onClick = onOpenSettings) {
                    Text(text = "Настр.", fontSize = scaleSp(13f))
                }
            }
        }

        Spacer(Modifier.height(scaleDp(12f)))

        VideoCard(
            title = "Видео анкета",
            subtitle = profile.videoTitle,
            scaleDp = scaleDp,
            scaleSp = scaleSp,
            onPreview = onOpenPreview,
            onUploadNew = onOpenChangeVideo,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(scaleDp(18f)))
    }
}

@Composable
private fun ProfilePreviewScreen(
    profile: UserProfileUi,
    onBack: () -> Unit,
    scaleDp: (Float) -> Dp,
    scaleSp: (Float) -> TextUnit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color(0xFF0B1220))
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

        // Полноэкранный "видеоплеер" пока как заглушка.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(scaleDp(18f)))
                .background(Color(0xFF101827)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Видео: ${profile.videoTitle}",
                fontSize = scaleSp(14f),
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun Avatar(
    name: String,
    size: Dp,
    seed: Int,
    scaleSp: (Float) -> TextUnit,
    modifier: Modifier = Modifier
) {
    val bg = when (seed % 5) {
        0 -> Color(0xFF2563EB) // blue
        1 -> Color(0xFF7C3AED) // violet
        2 -> Color(0xFFDB2777) // pink
        3 -> Color(0xFF059669) // green
        else -> Color(0xFFF59E0B) // amber
    }.copy(alpha = 0.26f)
    val fg = when (seed % 5) {
        0 -> Color(0xFF93C5FD)
        1 -> Color(0xFFC4B5FD)
        2 -> Color(0xFFF9A8D4)
        3 -> Color(0xFF6EE7B7)
        else -> Color(0xFFFCD34D)
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
            color = fg
        )
    }
}

@Composable
private fun VideoCard(
    title: String,
    subtitle: String,
    onPreview: () -> Unit,
    onUploadNew: () -> Unit,
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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(scaleDp(180f))
                .clip(RoundedCornerShape(scaleDp(16f)))
                .background(Color(0xFF101827)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Превью видео",
                fontSize = scaleSp(14f),
                color = Color.White.copy(alpha = 0.9f)
            )
        }

        Spacer(Modifier.height(scaleDp(10f)))

        Text(
            text = subtitle,
            fontSize = scaleSp(12f),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.height(scaleDp(10f)))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(scaleDp(10f))
        ) {
            OutlinedButton(
                onClick = onPreview,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Просмотр", fontSize = scaleSp(13f))
            }
            Button(
                onClick = onUploadNew,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(text = "Изменить видео", fontSize = scaleSp(13f))
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
    var age by remember(initial.age) { mutableStateOf(initial.age) }
    var city by remember(initial.city) { mutableStateOf(initial.city) }
    var avatarSeed by remember(initial.avatarSeed) { mutableIntStateOf(initial.avatarSeed) }

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

            Text(
                text = "Аватар",
                fontSize = scaleSp(14f),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(scaleDp(8f)))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(
                    name = name.ifBlank { initial.name },
                    size = scaleDp(56f),
                    seed = avatarSeed,
                    scaleSp = scaleSp
                )
                Spacer(Modifier.width(scaleDp(12f)))
                OutlinedButton(onClick = { avatarSeed += 1 }) {
                    Text(text = "Изменить", fontSize = scaleSp(13f))
                }
                Spacer(Modifier.width(scaleDp(10f)))
                Text(
                    text = "Загрузка фото подключится позже",
                    fontSize = scaleSp(12f),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Spacer(Modifier.height(scaleDp(14f)))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Имя", fontSize = scaleSp(12f)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(scaleDp(10f)))
            OutlinedTextField(
                value = age,
                onValueChange = { age = it.filter { ch -> ch.isDigit() }.take(2) },
                label = { Text("Возраст", fontSize = scaleSp(12f)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(scaleDp(10f)))
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("Город", fontSize = scaleSp(12f)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(scaleDp(14f)))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(scaleDp(10f))
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Отмена", fontSize = scaleSp(13f))
                }
                Button(
                    onClick = {
                        onSave(
                            initial.copy(
                                name = name.trim().ifBlank { initial.name },
                                age = age.trim().ifBlank { initial.age },
                                city = city.trim().ifBlank { initial.city },
                                avatarSeed = avatarSeed
                            )
                        )
                    },
                    modifier = Modifier.weight(1f)
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
    profile: UserProfileUi,
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
                            "Другие видят твою анкету. Лента и сообщения доступны."
                        } else {
                            "Твою анкету не показываем другим. Лента и сообщения заблокированы."
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

            Button(onClick = onChangePassword) {
                Text(text = "Поменять пароль", fontSize = scaleSp(13f))
            }

            Spacer(Modifier.height(scaleDp(10f)))

            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
            ) {
                Text(text = "Выйти из аккаунта", fontSize = scaleSp(13f), color = Color.White)
            }

            Spacer(Modifier.height(scaleDp(18f)))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChangeVideoSheet(
    currentVideoTitle: String,
    onDismiss: () -> Unit,
    onUploadNewVideo: () -> Unit,
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

            // Единственное действие.
            Button(onClick = onUploadNewVideo) {
                Text(text = "Загрузить новое видео", fontSize = scaleSp(13f))
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

            OutlinedTextField(
                value = oldPass,
                onValueChange = { oldPass = it },
                label = { Text("Текущий пароль", fontSize = scaleSp(12f)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(scaleDp(10f)))
            OutlinedTextField(
                value = newPass,
                onValueChange = { newPass = it },
                label = { Text("Новый пароль", fontSize = scaleSp(12f)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(scaleDp(10f)))
            OutlinedTextField(
                value = confirm,
                onValueChange = { confirm = it },
                label = { Text("Повторите новый пароль", fontSize = scaleSp(12f)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(scaleDp(14f)))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(scaleDp(10f))
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Отмена", fontSize = scaleSp(13f))
                }
                Button(
                    onClick = { onDismiss() },
                    enabled = canSave,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Сохранить", fontSize = scaleSp(13f))
                }
            }

            Spacer(Modifier.height(scaleDp(18f)))
        }
    }
}

