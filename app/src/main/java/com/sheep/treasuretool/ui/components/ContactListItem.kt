package com.sheep.treasuretool.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sheep.treasuretool.data.model.Contact
import com.sheep.treasuretool.data.local.AvatarCache
import androidx.compose.foundation.clickable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.sheep.treasuretool.service.ContactService

@Composable
fun ContactListItem(
    contact: Contact,
    onClick: () -> Unit,
    avatarCache: AvatarCache,
    contactService: ContactService,
    modifier: Modifier = Modifier
) {

    val onlineStatus = remember { mutableStateOf(contact.isOnline) }
    val lastMessage = remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        contactService.getContactStatus(contact).collect {
            onlineStatus.value = it
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 头像
        Box(
            modifier = Modifier.size(48.dp)
        ) {
            ContactAvatar(
                userId = contact.userId,
                avatarCache = avatarCache
            )
            if (onlineStatus.value) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(8.dp)
                        .offset(y = (36).dp)
                )
            }
            // 未读消息数
            if (contact.unreadCount > 0) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp)
                ) {
                    Text(
                        text = if (contact.unreadCount > 99) "99+" else contact.unreadCount.toString(),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 联系人信息
        ContactInfo(
            contact = contact,
            modifier = Modifier.weight(1f)
        )
    }
} 