package com.sheep.treasuretool.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.SerialName

@Serializable
data class MessageFrame<T>(
    val type: FrameType,
    val data: T,
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        val json = Json {
            ignoreUnknownKeys = true // 忽略未知字段
            isLenient = true // 宽松解析
            encodeDefaults = true // 编码默认值
        }

        /**
         * 创建消息帧
         */
        inline fun <reified T> create(type: FrameType, data: T): String {
            return json.encodeToString(MessageFrame(type, data))
        }

        /**
         * 解析消息帧
         */
        inline fun <reified T> parse(frameString: String): MessageFrame<T>? {
            return try {
                json.decodeFromString<MessageFrame<T>>(frameString)
            } catch (e: Exception) {
                null
            }
        }
    }

}

@Serializable
enum class FrameType {
    @SerialName("CHAT_MESSAGE")
    CHAT_MESSAGE,   // 聊天消息
    @SerialName("READ_RECEIPT")
    READ_RECEIPT,   // 已读回执
    @SerialName("ONLINE_MESSAGE")
    ONLINE_MESSAGE  // 在线状态
}

@Serializable
enum class OnlineStatus {
    @SerialName("ONLINE")
    ONLINE,    // 在线
    @SerialName("OFFLINE")
    OFFLINE    // 离线
} 