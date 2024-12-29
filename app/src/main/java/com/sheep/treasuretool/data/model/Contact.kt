package com.sheep.treasuretool.data.model

import com.sheep.treasuretool.data.model.entity.ChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Contact(
    val userId: String,
    val type: Int,
    val name: String,
    val avatar: String,
    var status: OnlineStatus,
    @Transient
    val lastMessage: ChatMessage? = null,
    val unreadCount: Int = 0,
) {
    var statusFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)

    fun initStatusFlow() {
        statusFlow  = MutableStateFlow(isOnline)
    }

    // 是否群组联系人
    val isGroup: Boolean
        get() = type == 2

    val isOnline: Boolean
        get() = status == OnlineStatus.ONLINE

    // 用于显示的最后消息内容
    val displayLastMessage: String?
        get() = lastMessage?.content

    // 用于显示的最后消息时间
    val displayLastMessageTime: Long
        get() = lastMessage?.sendTime ?: 0L
}