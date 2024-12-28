package com.sheep.treasuretool.data.model.entity

import com.sheep.treasuretool.data.model.Contact
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Serializable
data class ChatMessage (
    val messageId: String = UUID.randomUUID().toString(),
    var messageType: Int,
    var isGroupMessage: Boolean,
    var content: String,
    var senderId: String,
    var senderName: String,
    var senderAvatar: String,
    var receiverId: String,
    val status: MessageStatus,
    val sendTime: Long
) {

    companion object {

        fun textMessage(content: String, sender: User, receiver: Contact): ChatMessage {
            return createMessage(MessageType.TEXT, content, sender, receiver)
        }

        fun imageMessage(content: String, sender: User, receiver: Contact): ChatMessage {
            return createMessage(MessageType.IMAGE, content, sender, receiver)
        }

        fun fileMessage(content: String, sender: User, receiver: Contact): ChatMessage {
            return createMessage(MessageType.FILE, content, sender, receiver)
        }

        private fun createMessage(messageType: MessageType, content: String, sender: User, receiver: Contact): ChatMessage {
            return ChatMessage(
                messageType = messageType.type,
                isGroupMessage = receiver.isGroup,
                content = content,
                senderId = sender.id,
                senderName = sender.nickname,
                senderAvatar = sender.avatar,
                receiverId = receiver.userId,
                status = MessageStatus.SENDING,
                sendTime = Instant.now().epochSecond
            )
        }
    }

    fun isFromMe(userId: String): Boolean {
        return senderId == userId
    }
}

@Serializable
enum class MessageType(val type: Int) {
    TEXT(1),
    IMAGE(2),
    FILE(3);


}

@Serializable
enum class MessageStatus {
    SENDING,
    SENT,
    READ,
    FAILED
} 