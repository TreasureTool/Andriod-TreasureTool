package com.sheep.treasuretool.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sheep.treasuretool.data.model.Contact
import com.sheep.treasuretool.data.local.AvatarCache
import com.sheep.treasuretool.data.local.ContactStore
import com.sheep.treasuretool.ui.components.ContactListItem
import org.koin.compose.koinInject

@Composable
fun ContactListScreen(
    onContactClick: (Contact) -> Unit = {},
    modifier: Modifier
) {
    val contactStore: ContactStore = koinInject()
    val avatarCache: AvatarCache = koinInject()
    var contacts by remember { mutableStateOf<List<Contact>>(emptyList()) }
    
    // 监听联系人列表更新
    LaunchedEffect(Unit) {
        contactStore.contacts.collect { updatedContacts ->
            contacts = updatedContacts
        }
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(
            items = contacts,
            key = { it.userId }
        ) { contact ->
            ContactListItem(
                contact = contact,
                onClick = { onContactClick(contact) },
                avatarCache = avatarCache
            )
            if (contacts.indexOf(contact) < contacts.size - 1) {
                HorizontalDivider(
                    modifier = Modifier.padding(start = 72.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )
            }
        }
    }
} 