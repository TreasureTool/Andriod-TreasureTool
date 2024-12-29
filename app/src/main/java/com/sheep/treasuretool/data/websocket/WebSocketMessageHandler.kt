package com.sheep.treasuretool.data.websocket

import android.util.Log
import com.sheep.treasuretool.data.local.ContactStore
import com.sheep.treasuretool.data.local.MessageStore
import com.sheep.treasuretool.data.model.FrameType
import com.sheep.treasuretool.data.model.MessageFrame
import com.sheep.treasuretool.data.model.OnlineMessage
import com.sheep.treasuretool.data.model.entity.ChatMessage
import com.sheep.treasuretool.service.ContactService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement

class WebSocketMessageHandler(
    private val messageStore: MessageStore,
    private val contactService: ContactService
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun handleMessage(frame: MessageFrame<JsonElement>) {
        Log.d("handleMessage", "$frame")
            when (frame.type) {
                FrameType.CHAT_MESSAGE -> handleChatMessage(frame)
                FrameType.ONLINE_MESSAGE -> handleOnlineMessage(frame)
                else -> Log.i("WebSocket", "未知消息类型: ${frame.type}")
            }
    }

    private fun handleChatMessage(frame: MessageFrame<JsonElement>) {
        scope.launch {
            try {
                val chatMessage = MessageFrame.json.decodeFromJsonElement<ChatMessage>(frame.data)
                // 保存消息到本地缓存
                messageStore.saveMessage(chatMessage)
            } catch (e: Exception) {
                Log.e("WebSocket", "解析聊天消息失败", e)
            }
        }
    }

    private fun handleOnlineMessage(frame: MessageFrame<JsonElement>) {
        scope.launch {
            try {
                val message = MessageFrame.json.decodeFromJsonElement<OnlineMessage>(frame.data)
                Log.d("WebSocket", "用户在线状态更新: ${message.userId} -> ${message.status}")
                contactService.updateContactStatus(message.userId, message.status)
            } catch (e: Exception) {
                Log.e("WebSocket", "解析在线状态消息失败", e)
            }
        }
    }
}