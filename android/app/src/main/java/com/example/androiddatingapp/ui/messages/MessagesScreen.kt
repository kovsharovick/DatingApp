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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.androiddatingapp.data.api.dto.MessageDto
import com.example.androiddatingapp.data.api.stomp.StompChatClient
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.example.androiddatingapp.ui.components.UserAvatar
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
@Composable
fun rememberInboxHasUnread(
    loadMatches: suspend () -> Result<List<ChatUi>>,
): Boolean {
    var hasUnread by remember { mutableStateOf(false) }
    LaunchedEffect(loadMatches) {
        loadMatches()
            .onSuccess { chats -> hasUnread = chats.any { it.unreadCount > 0 } }
    }
    return hasUnread
}

@Composable
fun MessagesScreen(
    currentUserId: Long?,
    loadMatches: suspend () -> Result<List<ChatUi>>,
    loadMessages: suspend (matchId: Long, currentUserId: Long?) -> Result<List<MessageUi>>,
    stompChatClient: StompChatClient,
    onIncomingMessage: (matchId: Long, MessageDto) -> MessageUi,
    scaleDp: (Float) -> Dp,
    scaleSp: (Float) -> TextUnit,
    modifier: Modifier = Modifier,
) {
    var notifications by remember { mutableStateOf<List<NotificationUi>>(emptyList()) }
    var readNotificationIds by remember { mutableStateOf(setOf<String>()) }

    var chats by remember { mutableStateOf<List<ChatUi>>(emptyList()) }
    var chatsLoading by remember { mutableStateOf(true) }
    var chatsError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(loadMatches) {
        chatsLoading = true
        chatsError = null
        loadMatches()
            .onSuccess { loaded ->
                chats = loaded
                notifications = loaded.map { chat ->
                    NotificationUi(
                        id = "match_${chat.matchId}",
                        title = "Совпадение",
                        body = "У вас совпадение с ${chat.name}",
                        time = chat.time,
                        isRead = readNotificationIds.contains("match_${chat.matchId}"),
                    )
                }
            }
            .onFailure { chatsError = it.message }
        chatsLoading = false
    }

    var showNotifications by remember { mutableStateOf(false) }
    var selectedMatchId by remember { mutableLongStateOf(-1L) }
    val selectedChat = if (selectedMatchId >= 0) {
        chats.firstOrNull { it.matchId == selectedMatchId }
    } else {
        null
    }

    LaunchedEffect(selectedMatchId, currentUserId) {
        if (selectedMatchId < 0) return@LaunchedEffect
        loadMessages(selectedMatchId, currentUserId)
            .onSuccess { messages ->
                chats = chats.map { chat ->
                    if (chat.matchId == selectedMatchId) chat.copy(messages = messages) else chat
                }
            }
    }

    DisposableEffect(selectedMatchId, currentUserId) {
        if (selectedMatchId < 0) {
            onDispose { }
        } else {
            val disposable = stompChatClient.subscribeToMatch(selectedMatchId) { dto ->
                val ui = onIncomingMessage(selectedMatchId, dto)
                chats = chats.map { chat ->
                    if (chat.matchId != selectedMatchId) return@map chat
                    if (chat.messages.any { it.text == ui.text && it.time == ui.time && it.fromMe == ui.fromMe }) {
                        chat
                    } else {
                        chat.copy(
                            messages = chat.messages + ui,
                            lastMessage = ui.text,
                            time = ui.time,
                        )
                    }
                }
            }
            onDispose { disposable.dispose() }
        }
    }

    Box(modifier.fillMaxSize()) {
        Surface(Modifier.fillMaxSize()) {
            when {
                selectedChat != null -> ChatDetail(
                    chat = selectedChat,
                    onBack = { selectedMatchId = -1L },
                    onSend = { text ->
                        stompChatClient.sendMessage(selectedChat.matchId, text)
                    },
                    scaleDp = scaleDp,
                    scaleSp = scaleSp,
                    modifier = Modifier.fillMaxSize()
                )
                showNotifications -> NotificationsList(
                    notifications = notifications,
                    onBack = { showNotifications = false },
                    scaleDp = scaleDp,
                    scaleSp = scaleSp,
                    modifier = Modifier.fillMaxSize()
                )
                else -> InboxList(
                    notifications = notifications,
                    chats = chats,
                    chatsLoading = chatsLoading,
                    chatsError = chatsError,
                    onOpenNotifications = {
                        readNotificationIds = readNotificationIds + notifications
                            .filter { !it.isRead }
                            .map { it.id }
                            .toSet()
                        notifications = notifications.map { it.copy(isRead = true) }
                        showNotifications = true
                    },
                    onOpenChat = { selectedMatchId = it },
                    scaleDp = scaleDp,
                    scaleSp = scaleSp,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

private data class NotificationUi(
    val id: String,
    val title: String,
    val body: String,
    val time: String,
    val isRead: Boolean,
)

@Composable
private fun InboxList(
    notifications: List<NotificationUi>,
    chats: List<ChatUi>,
    chatsLoading: Boolean,
    chatsError: String?,
    onOpenNotifications: () -> Unit,
    onOpenChat: (Long) -> Unit,
    scaleDp: (Float) -> Dp,
    scaleSp: (Float) -> TextUnit,
    modifier: Modifier = Modifier
) {
    val unreadNotifications = notifications.count { !it.isRead }

    Column(modifier.background(MaterialTheme.colorScheme.background)) {
        Text(
            text = "Входящие",
            fontSize = scaleSp(20f),
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = scaleDp(16f), vertical = scaleDp(12f)),
            color = MaterialTheme.colorScheme.onBackground
        )

        LazyColumn(
            modifier = modifier,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = scaleDp(12f))
        ) {
            item(key = "notifications") {
                NotificationsEntryRow(
                    unreadCount = unreadNotifications,
                    onClick = onOpenNotifications,
                    scaleDp = scaleDp,
                    scaleSp = scaleSp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = scaleDp(12f), vertical = scaleDp(6f))
                )
            }
            if (chatsLoading) {
                item(key = "loading") {
                    Text(
                        text = "Загрузка чатов…",
                        fontSize = scaleSp(14f),
                        modifier = Modifier.padding(scaleDp(16f)),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    )
                }
            }
            if (chatsError != null) {
                item(key = "error") {
                    Text(
                        text = chatsError,
                        fontSize = scaleSp(14f),
                        modifier = Modifier.padding(scaleDp(16f)),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
            if (!chatsLoading && chats.isEmpty() && chatsError == null) {
                item(key = "empty") {
                    Text(
                        text = "Пока нет совпадений. Лайкайте анкеты в ленте.",
                        fontSize = scaleSp(14f),
                        modifier = Modifier.padding(scaleDp(16f)),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    )
                }
            }
            items(chats, key = { it.matchId }) { chat ->
                ChatRow(
                    chat = chat,
                    onClick = { onOpenChat(chat.matchId) },
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
private fun NotificationsEntryRow(
    unreadCount: Int,
    onClick: () -> Unit,
    scaleDp: (Float) -> Dp,
    scaleSp: (Float) -> TextUnit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(scaleDp(16f)))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(scaleDp(12f)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(scaleDp(44f))
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.22f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🔔",
                fontSize = scaleSp(18f)
            )
        }

        Spacer(Modifier.width(scaleDp(12f)))

        Column(Modifier.weight(1f)) {
            Text(
                text = "Уведомления",
                fontSize = scaleSp(16f),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(scaleDp(4f)))
            Text(
                text = if (unreadCount > 0) {
                    "Непрочитанных: $unreadCount"
                } else {
                    "Нет новых уведомлений"
                },
                fontSize = scaleSp(13f),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (unreadCount > 0) {
            Spacer(Modifier.width(scaleDp(10f)))
            UnreadDot(scaleDp = scaleDp)
        }
    }
}

@Composable
private fun NotificationsList(
    notifications: List<NotificationUi>,
    onBack: () -> Unit,
    scaleDp: (Float) -> Dp,
    scaleSp: (Float) -> TextUnit,
    modifier: Modifier = Modifier
) {
    Column(modifier.background(MaterialTheme.colorScheme.background)) {
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
                text = "Уведомления",
                fontSize = scaleSp(18f),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        LazyColumn(
            modifier = modifier,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = scaleDp(12f))
        ) {
            if (notifications.isEmpty()) {
                item(key = "empty_notifications") {
                    Text(
                        text = "Пока нет уведомлений о совпадениях.",
                        fontSize = scaleSp(14f),
                        modifier = Modifier.padding(scaleDp(16f)),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    )
                }
            }
            items(notifications, key = { it.id }) { notification ->
                NotificationRow(
                    notification = notification,
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
private fun NotificationRow(
    notification: NotificationUi,
    scaleDp: (Float) -> Dp,
    scaleSp: (Float) -> TextUnit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(scaleDp(16f)))
            .background(
                if (notification.isRead) {
                    MaterialTheme.colorScheme.surface
                } else {
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                }
            )
            .padding(scaleDp(12f)),
        verticalAlignment = Alignment.Top
    ) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = notification.title,
                    fontSize = scaleSp(15f),
                    fontWeight = if (notification.isRead) FontWeight.Medium else FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.width(scaleDp(8f)))
                Text(
                    text = notification.time,
                    fontSize = scaleSp(12f),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    maxLines = 1
                )
            }
            Spacer(Modifier.height(scaleDp(4f)))
            Text(
                text = notification.body,
                fontSize = scaleSp(13f),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (!notification.isRead) {
            Spacer(Modifier.width(scaleDp(10f)))
            UnreadDot(scaleDp = scaleDp)
        }
    }
}

@Composable
private fun UnreadDot(scaleDp: (Float) -> Dp) {
    Box(
        modifier = Modifier
            .size(scaleDp(8f))
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondary)
    )
}

@Composable
private fun ChatRow(
    chat: ChatUi,
    onClick: () -> Unit,
    scaleDp: (Float) -> Dp,
    scaleSp: (Float) -> TextUnit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(scaleDp(16f)))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(scaleDp(12f)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UserAvatar(
            name = chat.name,
            avatarUrl = chat.avatarUrl,
            size = scaleDp(44f),
            scaleSp = scaleSp,
        )

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
                            .background(MaterialTheme.colorScheme.secondary)
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
    onBack: () -> Unit,
    onSend: (String) -> Unit,
    scaleDp: (Float) -> Dp,
    scaleSp: (Float) -> TextUnit,
    modifier: Modifier = Modifier
) {
    var draft by remember { mutableStateOf("") }

    Column(modifier.background(MaterialTheme.colorScheme.background)) {
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
            UserAvatar(
                name = chat.name,
                avatarUrl = chat.avatarUrl,
                size = scaleDp(36f),
                scaleSp = scaleSp,
            )
            Spacer(Modifier.width(scaleDp(10f)))
            Text(
                text = chat.name,
                fontSize = scaleSp(18f),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(scaleDp(12f)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Сообщение…", fontSize = scaleSp(13f)) },
                singleLine = true,
            )
            Spacer(Modifier.width(scaleDp(10f)))
            Button(
                onClick = {
                    val text = draft.trim()
                    if (text.isNotEmpty()) {
                        onSend(text)
                        draft = ""
                    }
                },
                enabled = draft.isNotBlank()
            ) {
                Text("Отпр.", fontSize = scaleSp(13f))
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
