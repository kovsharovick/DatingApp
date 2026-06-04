package com.example.androiddatingapp.data.api.stomp

import com.example.androiddatingapp.BuildConfig
import com.example.androiddatingapp.data.api.dto.MessageDto
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.dto.StompHeader
import ua.naiksoftware.stomp.dto.StompMessage

/**
 * STOMP/SockJS чат через туннель loca.lt.
 * Заголовки `Authorization` и `bypass-tunnel-reminder` — на HTTP (SockJS) и в STOMP CONNECT.
 */
class StompChatClient(
    private val gson: Gson = Gson(),
) {
    private var stompClient: ua.naiksoftware.stomp.StompClient? = null
    private val subscriptions = CompositeDisposable()

    fun connect(token: String) {
        disconnect()

        val wsUrl = buildWebSocketUrl()
        val headers = tunnelAuthHeaders(token)
        val stompHeaders = headers.map { (key, value) -> StompHeader(key, value) }

        val client = Stomp.over(Stomp.ConnectionProvider.OKHTTP, wsUrl, headers)

        subscriptions.add(
            client.lifecycle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { },
        )

        client.connect(stompHeaders)

        stompClient = client
    }

    fun disconnect() {
        subscriptions.clear()
        stompClient?.disconnect()
        stompClient = null
    }

    fun subscribeToMatch(
        matchId: Long,
        onMessage: (MessageDto) -> Unit,
    ): Disposable {
        val client = stompClient ?: error("STOMP не подключён")
        val destination = "/topic/match/$matchId"
        return client.topic(destination)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { message: StompMessage ->
                parseMessage(message)?.let(onMessage)
            }
    }

    fun sendMessage(matchId: Long, content: String): Disposable {
        val client = stompClient ?: error("STOMP не подключён")
        val destination = "/app/chat/$matchId"
        val payload = gson.toJson(StompMessageRequest(matchId = matchId, content = content))
        return client.send(destination, payload)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ }, { })
    }

    private fun parseMessage(message: StompMessage): MessageDto? =
        runCatching { gson.fromJson(message.payload, MessageDto::class.java) }.getOrNull()

    companion object {
        fun buildWebSocketUrl(): String {
            val base = BuildConfig.API_BASE_URL.trim().trimEnd('/')
            val wsBase = base
                .replaceFirst("https://", "wss://")
                .replaceFirst("http://", "ws://")
            return "$wsBase/ws/websocket"
        }

        fun tunnelAuthHeaders(token: String): Map<String, String> = mapOf(
            "Authorization" to "Bearer $token",
            "bypass-tunnel-reminder" to "true",
        )
    }
}
