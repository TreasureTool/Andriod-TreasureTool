package com.sheep.treasuretool.data.repository

import android.util.Log
import com.sheep.treasuretool.data.api.ApiService
import com.sheep.treasuretool.data.local.MessageStore
import com.sheep.treasuretool.data.model.entity.ChatMessage
import com.sheep.treasuretool.data.model.entity.MessageStatus
import com.sheep.treasuretool.data.websocket.TreasureWebSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class ChatRepository(
    private val messageStore: MessageStore,
    private val webSocket: TreasureWebSocket
) {

    /**
     * 发送消息
     * 1. 保存到本地
     * 2. 通过WebSocket发送
     */
    suspend fun sendMessage(message: ChatMessage): Result<ChatMessage> = withContext(Dispatchers.IO) {
        try {
            // 1. 保存到本地
            messageStore.saveMessage(message)

            // 3. 通过WebSocket发送
            val result = suspendCancellableCoroutine { continuation ->
                webSocket.sendMessage(message) { result ->
                    continuation.resume(result)
                }
            }
            // 4. 更新本地消息状态
            result.onSuccess {
                messageStore.updateMessageStatus(message.receiverId, message.messageId, MessageStatus.SENT )
            }
                .onFailure {
                messageStore.updateMessageStatus(message.receiverId, message.messageId, MessageStatus.FAILED )
            }
            result
        } catch (e: Exception) {
            Log.e("ChatRepository", "发送消息失败", e)
            Result.failure(e)
        }
    }

    /**
     * 加载历史消息
     * @param userId 当前用户Id
     * @param contactId 对方Id
     * @param offset 偏移量
     * @param limit 每次加载的消息数量
     */
    suspend fun loadHistoryMessages(
        userId: String,
        contactId: String,
        offset: Int,
        limit: Int = 20
    ): Result<List<ChatMessage>> = withContext(Dispatchers.IO) {
        try {
            val response = ApiService.messageApi.getMessageHistory(
                userId = userId,
                contactId = contactId,
                offset = offset,
                limit = limit
            )

            if (response.success && response.data != null) {
                // 保存到本地缓存
                response.data.forEach { message ->
                    messageStore.saveMessage(message)
                }
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "加载历史消息失败"))
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "加载历史消息失败", e)
            Result.failure(e)
        }
    }

    suspend fun clearMessages(contactId: String) {
        messageStore.clearMessages(contactId)
    }
}

