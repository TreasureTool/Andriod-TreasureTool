package com.sheep.treasuretool.data.websocket

import android.util.Log
import com.sheep.treasuretool.data.local.UserPreferences
import com.sheep.treasuretool.data.model.*
import com.sheep.treasuretool.data.model.entity.ChatMessage
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import okhttp3.*
import java.util.concurrent.TimeUnit

class TreasureWebSocket(
    private val baseUrl: String,
    private val userPreferences: UserPreferences,
    private val webSocketMessageHandler: WebSocketMessageHandler,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .pingInterval(30, TimeUnit.SECONDS)
        .build()
) {
    private var webSocket: WebSocket? = null
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun connect() {
        if (_connectionState.value is ConnectionState.Connected) return

        scope.launch {
            try {
                val currentUser = userPreferences.currentUser.first()
                val request = Request.Builder()
                    .url("$baseUrl?userId=${currentUser.id}")
                    .build()

                webSocket = client.newWebSocket(request, object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        _connectionState.value = ConnectionState.Connected
                        Log.d("WebSocket", "连接成功: userId=${currentUser.id}")
                    }

                    override fun onMessage(webSocket: WebSocket, text: String) {
                        try {
                            val frame = MessageFrame.json.decodeFromString<MessageFrame<JsonElement>>(text)
                            webSocketMessageHandler.handleMessage(frame)
                        } catch (e: Exception) {
                            Log.e("WebSocket", "消息处理失败: $text", e)
                        }
                    }

                    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                        webSocket.close(1000, null)
                        _connectionState.value = ConnectionState.Disconnected
                        Log.d("WebSocket", "连接关闭中: $reason")
                    }

                    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                        _connectionState.value = ConnectionState.Error(t.message ?: "连接失败")
                        Log.e("WebSocket", "连接失败", t)
                        retryConnection()
                    }
                })
            } catch (e: Exception) {
                Log.e("WebSocket", "连接失败: ${e.message}")
                _connectionState.value = ConnectionState.Error(e.message ?: "连接失败")
            }
        }
    }

    fun sendMessage(message: ChatMessage, callback: (Result<ChatMessage>) -> Unit = {}) {
        try {
            val frame = MessageFrame(
                type = FrameType.CHAT_MESSAGE,
                data = message
            )
            val frameJson = MessageFrame.json.encodeToString(frame)
            
            if (_connectionState.value is ConnectionState.Connected) {
                webSocket?.send(frameJson)
            } else {
                // 如果未连接，先尝试重连
                connect()
                callback(Result.failure(Exception("未连接到服务器")))
            }
        } catch (e: Exception) {
            Log.e("WebSocket", "消息发送失败", e)
            callback(Result.failure(e))
        }
    }

    private fun retryConnection() {
        scope.launch {
            delay(5000) // 5秒后重试
            connect()
        }
    }

    fun disconnect() {
        webSocket?.close(1000, null)
        webSocket = null
        _connectionState.value = ConnectionState.Disconnected
        scope.cancel()
    }

    sealed class ConnectionState {
        data object Connected : ConnectionState()
        data object Disconnected : ConnectionState()
        data class Error(val message: String) : ConnectionState()
    }
} 