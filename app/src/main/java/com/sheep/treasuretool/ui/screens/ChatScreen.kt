package com.sheep.treasuretool.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sheep.treasuretool.data.model.entity.ChatMessage
import com.sheep.treasuretool.data.model.Contact
import com.sheep.treasuretool.data.local.AvatarCache
import com.sheep.treasuretool.data.local.MessageStore
import com.sheep.treasuretool.data.local.UserPreferences
import com.sheep.treasuretool.data.model.entity.MessageStatus
import com.sheep.treasuretool.data.repository.ChatRepository
import com.sheep.treasuretool.ui.components.ChatMessageItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import com.sheep.treasuretool.data.model.entity.User
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    contact: Contact,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val avatarCache: AvatarCache = koinInject()
    val userPreferences: UserPreferences = koinInject()
    val chatRepository: ChatRepository = koinInject()
    val messageStore: MessageStore = koinInject()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var messageText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var isLoading by remember { mutableStateOf(false) }
    var isSending by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    var currentUser by remember { mutableStateOf<User?>(null) }
    var pendingMessage by remember { mutableStateOf<String?>(null) }
    var offset by remember { mutableIntStateOf(0) }
    var hasMore by remember { mutableStateOf(true) }

    // 初始化数据
    LaunchedEffect(Unit) {
        currentUser = userPreferences.currentUser.first()
        messageStore.getMessages(contact).collect { updatedMessages ->
            messages = updatedMessages
            offset = updatedMessages.size
            // 更新未读消息状态
            updatedMessages.filter { !it.isFromMe(currentUser!!.id) && it.status != MessageStatus.READ }
                .forEach { message ->
                    messageStore.updateMessageStatus(
                        contact.userId,
                        message.messageId,
                        MessageStatus.READ
                    )
                }
        }
    }

    // 处理待发送的消息
    LaunchedEffect(pendingMessage) {
        pendingMessage?.let { text ->
            try {
                isSending = true
                val newMessage = ChatMessage.textMessage(
                    content = text,
                    sender = currentUser!!,
                    receiver = contact
                )
                chatRepository.sendMessage(newMessage)
            } finally {
                isSending = false
                pendingMessage = null
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = contact.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 17.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            SwipeRefresh(
                state = rememberSwipeRefreshState(isLoading),
                onRefresh = {
                    scope.launch {
                        if (!isLoading && hasMore) {
                            try {
                                isLoading = true
                                chatRepository.loadHistoryMessages(
                                    userId = currentUser!!.id,
                                    contactId = contact.userId,
                                    offset = offset
                                ).onSuccess { historyMessages ->
                                    hasMore = historyMessages.isNotEmpty()
                                }.onFailure {
                                    snackbarHostState.showSnackbar("加载历史消息失败")
                                }
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                ) {
                    // Messages list
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        reverseLayout = false,
                        state = listState
                    ) {
                        items(messages.reversed()) { message ->
                            ChatMessageItem(
                                message = message,
                                avatarCache = avatarCache,
                                currentUser = currentUser!!,
                                contact = contact
                            )
                        }
                    }

                    // Input field
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth(),
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .windowInsetsPadding(
                                    WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)
                                )
                                .padding(
                                    start = 8.dp,
                                    end = 8.dp,
                                    top = 12.dp,
                                    bottom = 0.dp
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextField(
                                value = messageText,
                                onValueChange = { messageText = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                                    .padding(vertical = 4.dp),
                                placeholder = { Text("输入消息") },
                                enabled = !isSending,
                                colors = TextFieldDefaults.textFieldColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                ),
                                shape = RoundedCornerShape(24.dp)
                            )
                            FloatingActionButton(
                                onClick = {
                                    if (messageText.isNotBlank() && !isSending) {
                                        pendingMessage = messageText
                                        messageText = ""
                                    }
                                },
                                modifier = Modifier.size(48.dp),
                                containerColor = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            ) {
                                if (isSending) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "发送",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
