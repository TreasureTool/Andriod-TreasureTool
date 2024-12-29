package com.sheep.treasuretool.data.model.entity

import com.sheep.treasuretool.data.model.Contact
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Serializable
data class ChatMessage (
    val messageId: String = UUID.randomUUID().toString(),   // 消息唯一id
    var messageType: Int,  // MessageType 枚举类型
    var isGroupMessage: Boolean,    // 是否群消息
    var content: String,    // 消息内容
    var senderId: String,   // 消息发送人id
    var senderName: String, // 消息发送人名称
    var senderAvatar: String, // 消息发送人头像
    var receiverId: String, // 消息接收人id
    val status: MessageStatus,  // 消息状态
    val sendTime: Long  // 消息发送时间
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