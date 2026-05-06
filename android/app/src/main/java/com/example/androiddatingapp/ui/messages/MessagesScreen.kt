package com.example.androiddatingapp.ui.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
fun MessagesScreen(
    isProfileActive: Boolean,
    onOpenSettings: () -> Unit,
    scaleDp: (Float) -> Dp,
    scaleSp: (Float) -> TextUnit,
    modifier: Modifier = Modifier
) {
    val chats = remember {
        listOf(
            ChatUi(
                id = "c1",
                name = "София",
                lastMessage = "Ты знаешь что такое 67?",
                time = "10:14",
                unreadCount = 2,
                messages = listOf(
                    MessageUi(fromMe = false, text = "Привет!", time = "10:14"),
                    MessageUi(fromMe = false, text = "Ты знаешь что такое 67?", time = "10:15")
                )
            ),
            ChatUi(
                id = "c2",
                name = "Мария",
                lastMessage = "Окей, давай вечером созвонимся.",
                time = "Вчера",
                unreadCount = 0,
                messages = listOf(
                    MessageUi(fromMe = true, text = "Абоба.", time = "19:03"),
                    MessageUi(fromMe = false, text = "Окей, давай вечером созвонимся.", time = "19:05")
                )
            ),
            ChatUi(
                id = "c3",
                name = "Екатерина",
                lastMessage = "Ты из какого района?",
                time = "Пн",
                unreadCount = 1,
                messages = listOf(
                    MessageUi(fromMe = false, text = "Ты из какого района?", time = "12:40")
                )
            )
        )
    }

    var selectedChatId by remember { mutableStateOf<String?>(null) }
    val selectedChat = chats.firstOrNull { it.id == selectedChatId }

    Box(modifier.fillMaxSize()) {
        Surface(Modifier.fillMaxSize()) {
            if (selectedChat == null) {
                ChatsList(
                    chats = chats,
                    enabled = isProfileActive,
                    onOpenChat = { selectedChatId = it },
                    scaleDp = scaleDp,
                    scaleSp = scaleSp,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                ChatDetail(
                    chat = selectedChat,
                    enabled = isProfileActive,
                    onBack = { selectedChatId = null },
                    scaleDp = scaleDp,
                    scaleSp = scaleSp,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        if (!isProfileActive) {
            PausedOverlay(
                scaleDp = scaleDp,
                scaleSp = scaleSp,
                onOpenSettings = onOpenSettings,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

private data class ChatUi(
    val id: String,
    val name: String,
    val lastMessage: String,
    val time: String,
    val unreadCount: Int,
    val messages: List<MessageUi>
)

private data class MessageUi(
    val fromMe: Boolean,
    val text: String,
    val time: String
)

@Composable
private fun ChatsList(
    chats: List<ChatUi>,
    enabled: Boolean,
    onOpenChat: (String) -> Unit,
    scaleDp: (Float) -> Dp,
    scaleSp: (Float) -> TextUnit,
    modifier: Modifier = Modifier
) {
    Column(modifier.background(MaterialTheme.colorScheme.background)) {
        Text(
            text = "Сообщения",
            fontSize = scaleSp(20f),
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = scaleDp(16f), vertical = scaleDp(12f)),
            color = MaterialTheme.colorScheme.onBackground
        )

        LazyColumn(
            modifier = modifier,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = scaleDp(12f))
        ) {
            items(chats, key = { it.id }) { chat ->
                ChatRow(
                    chat = chat,
                    enabled = enabled,
                    onClick = { onOpenChat(chat.id) },
                    scaleDp = scaleDp,
                    scaleSp = scaleSp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = scaleDp(12f), vertical = scaleDp(6f))
                )
            }
        }
    }
}

@Composable
private fun ChatRow(
    chat: ChatUi,
    enabled: Boolean,
    onClick: () -> Unit,
    scaleDp: (Float) -> Dp,
    scaleSp: (Float) -> TextUnit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(scaleDp(16f)))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(scaleDp(12f)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Аватар-заглушка (первая буква имени)
        Box(
            modifier = Modifier
                .size(scaleDp(44f))
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = chat.name.take(1),
                fontSize = scaleSp(16f),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(Modifier.width(scaleDp(12f)))

        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = chat.name,
                    fontSize = scaleSp(16f),
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.width(scaleDp(8f)))
                Text(
                    text = chat.time,
                    fontSize = scaleSp(12f),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    maxLines = 1
                )
            }
            Spacer(Modifier.height(scaleDp(4f)))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = chat.lastMessage,
                    fontSize = scaleSp(13f),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (chat.unreadCount > 0) {
                    Spacer(Modifier.width(scaleDp(10f)))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444))
                            .padding(horizontal = scaleDp(8f), vertical = scaleDp(3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = chat.unreadCount.toString(),
                            fontSize = scaleSp(12f),
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatDetail(
    chat: ChatUi,
    enabled: Boolean,
    onBack: () -> Unit,
    scaleDp: (Float) -> Dp,
    scaleSp: (Float) -> TextUnit,
    modifier: Modifier = Modifier
) {
    var draft by remember { mutableStateOf("") }

    Column(modifier.background(MaterialTheme.colorScheme.background)) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = scaleDp(12f), vertical = scaleDp(10f)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "←",
                fontSize = scaleSp(20f),
                modifier = Modifier
                    .clip(RoundedCornerShape(scaleDp(10f)))
                    .clickable { onBack() }
                    .padding(horizontal = scaleDp(10f), vertical = scaleDp(6f)),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.width(scaleDp(10f)))
            Text(
                text = chat.name,
                fontSize = scaleSp(18f),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Messages
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = scaleDp(12f)),
            verticalArrangement = Arrangement.spacedBy(scaleDp(8f))
        ) {
            items(chat.messages) { msg ->
                MessageBubble(
                    message = msg,
                    scaleDp = scaleDp,
                    scaleSp = scaleSp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Composer (локальная заглушка: ввод не сохраняем в чат, чтобы не усложнять state)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(scaleDp(12f)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = draft,
                onValueChange = { if (enabled) draft = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Сообщение…", fontSize = scaleSp(13f)) },
                singleLine = true,
                enabled = enabled
            )
            Spacer(Modifier.width(scaleDp(10f)))
            Button(
                onClick = { draft = "" },
                enabled = enabled && draft.isNotBlank()
            ) {
                Text("Отпр.", fontSize = scaleSp(13f))
            }
        }
    }
}

@Composable
private fun PausedOverlay(
    scaleDp: (Float) -> Dp,
    scaleSp: (Float) -> TextUnit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable(enabled = true, onClick = { /* consume */ }),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(scaleDp(16f))
                .clip(RoundedCornerShape(scaleDp(18f)))
                .background(Color(0xFF101827))
                .padding(scaleDp(16f)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Анкета на паузе",
                fontSize = scaleSp(16f),
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Spacer(Modifier.height(scaleDp(8f)))
            Text(
                text = "Возобнови показ анкеты в настройках, чтобы снова писать сообщения.",
                fontSize = scaleSp(13f),
                color = Color.White.copy(alpha = 0.85f)
            )
            Spacer(Modifier.height(scaleDp(12f)))
            Button(
                onClick = onOpenSettings,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
            ) {
                Text(text = "Перейти в настройки", fontSize = scaleSp(13f), color = Color.White)
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: MessageUi,
    scaleDp: (Float) -> Dp,
    scaleSp: (Float) -> TextUnit,
    modifier: Modifier = Modifier
) {
    val bubbleColor =
        if (message.fromMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val textColor =
        if (message.fromMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val align = if (message.fromMe) Alignment.CenterEnd else Alignment.CenterStart

    Box(modifier, contentAlignment = align) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.86f)
                .clip(RoundedCornerShape(scaleDp(16f)))
                .background(bubbleColor)
                .padding(horizontal = scaleDp(12f), vertical = scaleDp(10f))
        ) {
            Text(
                text = message.text,
                fontSize = scaleSp(14f),
                color = textColor
            )
            Spacer(Modifier.height(scaleDp(4f)))
            Text(
                text = message.time,
                fontSize = scaleSp(11f),
                color = textColor.copy(alpha = 0.7f)
            )
        }
    }
}

