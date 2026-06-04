package com.example.androiddatingapp.ui.messages

data class ChatUi(
    val matchId: Long,
    val name: String,
    val avatarUrl: String = "",
    val lastMessage: String,
    val time: String,
    val unreadCount: Int,
    val messages: List<MessageUi>,
)

data class MessageUi(
    val fromMe: Boolean,
    val text: String,
    val time: String,
)
