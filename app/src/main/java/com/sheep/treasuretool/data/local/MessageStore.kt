package com.sheep.treasuretool.data.local

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.sheep.treasuretool.data.model.Contact
import com.sheep.treasuretool.data.model.entity.ChatMessage
import com.sheep.treasuretool.data.model.entity.MessageStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MessageStore(
    private val context: Context,
    private val userPreferences: UserPreferences,
) {
    companion object {
        private const val MAX_MESSAGES_PER_CHAT = 500  // 每个聊天最多存储500条消息
        private val Context.messageDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_contact_messages")
    }

    /**
     * 获取指定聊天对象的消息流
     * @param contact 聊天对象
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getMessages(contact: Contact): Flow<List<ChatMessage>> {
        return userPreferences.currentUser.flatMapLatest { user ->
            run {
                val key = stringPreferencesKey("${user.id}_${contact.userId}")
                context.messageDataStore.data.map { preferences ->
                    val json = preferences[key] ?: "[]"
                    Json.decodeFromString<List<ChatMessage>>(json)
                        .distinctBy { it.messageId }
                        .sortedByDescending { it.sendTime }
                }
            }
        }.flowOn(Dispatchers.IO)
    }


    /**
     * 保存消息
     * 使用{currentUser.id}_{contact.id}作为key来存储消息
     */
    suspend fun saveMessage(message: ChatMessage) {
        val currentUser = userPreferences.currentUser.first()
        // 发送人为当前用户， 使用接收人作为存储key
        // 消息接收对象为群组， 发送人不为当前用户， 也能看到其他人发在群里的消息， 这里也要使用群id作为联系人key存储
        val contactId = if (message.receiverId == currentUser.id) {
            message.senderId
        } else {
            message.receiverId
        }
        val key = stringPreferencesKey("${currentUser.id}_${contactId}")
        context.messageDataStore.edit { preferences ->
            val existingJson = preferences[key] ?: "[]"
            val existingMessages = Json.decodeFromString<List<ChatMessage>>(existingJson)
            // 确保不会添加重复的消息
            val updatedMessages = if (existingMessages.any { it.messageId == message.messageId }) {
                existingMessages.map {
                    if (it.messageId == message.messageId) message else it
                }
            } else {
                listOf(message) + existingMessages
            }
                .take(MAX_MESSAGES_PER_CHAT) // 限制消息数量
            val encodeToString = Json.encodeToString(updatedMessages)
            preferences[key] = encodeToString
//            val count = updatedMessages.sumOf {
//                if (it.status == MessageStatus.SENT && !it.isFromMe(currentUser.id)) {
//                    1
//                } else {
//                    0.toInt()
//                }
//            }
        }
    }

    /**
     * 更新消息状态
     */
    suspend fun updateMessageStatus(contactId: String, messageId: String, status: MessageStatus) {
        Log.d("sendMessage debug", "更新消息状态 ---> contactId = $contactId，  messageId = $messageId，  status = $status")
        val currentUser = userPreferences.currentUser.first()
        val key = stringPreferencesKey("${currentUser.id}_$contactId")
        context.messageDataStore.edit { preferences ->
            val json = preferences[key] ?: return@edit
            val messages = Json.decodeFromString<List<ChatMessage>>(json)
            val updatedMessages = messages.map { message ->
                if (message.messageId == messageId) {
                    message.copy(status = status)
                } else {
                    message
                }
            }
            preferences[key] = Json.encodeToString(updatedMessages)
        }
    }

    /**
     * 清除指定聊天对象的消息
     */
    suspend fun clearMessages(contactId: String) {
        val currentUser = userPreferences.currentUser.first() ?: return
        val key = stringPreferencesKey("${currentUser.id}_$contactId")
        context.messageDataStore.edit { preferences ->
            preferences.remove(key)
        }
    }
} 