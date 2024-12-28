package com.sheep.treasuretool.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sheep.treasuretool.data.local.AvatarCache
import com.sheep.treasuretool.data.local.MessageStore
import com.sheep.treasuretool.data.local.UserPreferences
import com.sheep.treasuretool.data.local.ContactStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.forEach
import org.koin.compose.koinInject
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier
) {
    val userPreferences: UserPreferences = koinInject()
    val avatarCache: AvatarCache = koinInject()
    val messageStore: MessageStore = koinInject()
    val contactStore: ContactStore = koinInject()
    val scope = rememberCoroutineScope()
    var nickname by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        nickname = userPreferences.currentUser.first().nickname
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = nickname,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            // 清除缓存按钮
            FilledTonalButton(
                onClick = {
                    scope.launch {
                        try {
                            isLoading = true
                            // 1. 清除头像缓存
                            avatarCache.clearCache()
                            // 2. 清除消息缓存
                            contactStore.contacts.first().forEach { contact->
                                messageStore.clearMessages(contact.userId)
                            }
                            // 3. 清除联系人缓存
                            contactStore.clearCache()
                            snackbarHostState.showSnackbar("缓存清除成功")
                        } catch (e: Exception) {
                            snackbarHostState.showSnackbar("清除缓存失败: ${e.message}")
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("清除缓存")
                }
            }

            // 退出登录按钮
            OutlinedButton(
                onClick = {
                    scope.launch {
                        userPreferences.clearUserLogin()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("退出登录")
            }
        }
    }
} 