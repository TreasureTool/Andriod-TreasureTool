package com.sheep.treasuretool.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.sheep.treasuretool.data.local.AvatarCache

@Composable
fun ContactAvatar(
    userId: String,
    avatarCache: AvatarCache,
    modifier: Modifier = Modifier
) {
    val bitmap = remember(userId) {
        avatarCache.getBitmapSync(userId)
    }

    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        AsyncImage(
            model = bitmap,
            contentDescription = "头像",
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop
        )
    }
} 