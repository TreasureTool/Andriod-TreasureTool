package com.sheep.treasuretool.ui.components

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sheep.treasuretool.data.local.AvatarCache
import com.sheep.treasuretool.data.model.Contact
import com.sheep.treasuretool.data.model.entity.ChatMessage
import com.sheep.treasuretool.data.model.entity.User
import com.sheep.treasuretool.ui.utils.TimeStampFormatter


@Composable
fun ChatMessageItem(
    message: ChatMessage,
    avatarCache: AvatarCache,
    currentUser: User,
    contact: Contact,
    modifier: Modifier = Modifier
) {
    val isFromMe = remember(message.senderId, currentUser.id) {
        message.isFromMe(currentUser.id)
    }

    val messageContent = remember(message.content) {
        message.content
    }

    val messageTime = remember(message.sendTime) {
        TimeStampFormatter.formatSendTime(message.sendTime)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .then(if (isFromMe) Modifier else Modifier),
        horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start
    ) {
        if (!isFromMe) {
            ContactAvatar(
                userId = currentUser.id,
                avatarCache = avatarCache,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isFromMe) Alignment.End else Alignment.Start,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            // 昵称和时间
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 2.dp),
                horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start
            ) {
                if (!isFromMe) {
                    Text(
                        text = contact.name,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                Text(
                    text = messageTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            Surface(
                color = if (isFromMe) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.primaryContainer
                },
                shape = RoundedCornerShape(
                    topStart = if (isFromMe) 16.dp else 4.dp,
                    topEnd = if (isFromMe) 4.dp else 16.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                ),
                shadowElevation = 1.dp
            ) {
                Text(
                    text = messageContent,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 15.sp
                    ),
                    color = if (isFromMe)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.primary
                )
            }
        }

        if (isFromMe) {
            Spacer(modifier = Modifier.width(8.dp))
            ContactAvatar(
                userId = currentUser.id,
                avatarCache = avatarCache,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}