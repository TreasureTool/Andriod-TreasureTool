package com.sheep.treasuretool.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sheep.treasuretool.R
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.sheep.treasuretool.data.repository.AuthRepository
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardActions
import com.sheep.treasuretool.data.repository.UserRepository
import com.sheep.treasuretool.ui.utils.keyboardVisibilityListener
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier
) {
    val authRepository: AuthRepository = koinInject()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    // 焦点管理
    val focusManager = LocalFocusManager.current
    val usernameFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    
    // 输入框交互状态
    val usernameInteractionSource = remember { MutableInteractionSource() }
    val passwordInteractionSource = remember { MutableInteractionSource() }
    val isUsernameFocused = usernameInteractionSource.collectIsFocusedAsState()
    val isPasswordFocused = passwordInteractionSource.collectIsFocusedAsState()

    // 添加一个状态来控制登录按钮的禁用
    var isLoginButtonEnabled by remember { mutableStateOf(true) }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            // 添加点击事件，点击空白区域时清除焦点
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null // 移除点击效果
            ) {
                focusManager.clearFocus()
            }
    ) {
        // 渐变背景
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .keyboardVisibilityListener(focusManager)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* 不做任何事，只是阻止事件传递 */ },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            
            // Logo 动画
            AnimatedVisibility(
                visible = !isUsernameFocused.value && !isPasswordFocused.value,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 标题动画
            AnimatedVisibility(
                visible = !isUsernameFocused.value && !isPasswordFocused.value,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "欢迎回来",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Text(
                        text = "请登录您的账号",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
                    )
                }
            }

            // 用户名输入框
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("用户名") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "用户名图标",
                        tint = if (isUsernameFocused.value) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(usernameFocusRequester),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        passwordFocusRequester.requestFocus() // 跳转到密码输入框
                    }
                ),
                interactionSource = usernameInteractionSource,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 密码输入框
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("密码") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "密码图标",
                        tint = if (isPasswordFocused.value) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = { passwordVisible = !passwordVisible },
                        enabled = !isLoading
                    ) {
                        Icon(
                            imageVector = if (passwordVisible) 
                                Icons.Default.VisibilityOff 
                            else 
                                Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "隐藏密码" else "显示密码",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                visualTransformation = if (passwordVisible) 
                    VisualTransformation.None 
                else 
                    PasswordVisualTransformation(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(passwordFocusRequester),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus() // ��除焦点，关闭键盘
                    },
                    onPrevious = {
                        focusManager.clearFocus() // 清除焦点，关闭键盘
                    }
                ),
                interactionSource = passwordInteractionSource,
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 忘记密码按钮
            TextButton(
                onClick = { /* TODO: 处理忘记密码 */ },
                modifier = Modifier.align(Alignment.End),
                enabled = !isLoading
            ) {
                Text("忘记密码?")
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 登录按钮
            Button(
                onClick = {
                    focusManager.clearFocus()
                    isLoginButtonEnabled = false  // 禁用按钮
                    coroutineScope.launch {
                        try {
                            val result = authRepository.login(username, password)
                            result.fold(
                                onSuccess = { response ->
                                    if (response.success) {
                                        snackbarHostState.showSnackbar(
                                            message = "登录成功",
                                            duration = SnackbarDuration.Short,
                                            withDismissAction = false
                                        )
                                    } else {
                                        snackbarHostState.showSnackbar(
                                            message = response.message?.takeIf { it.length <= 20 } ?: "登录失败",
                                            duration = SnackbarDuration.Long,
                                            withDismissAction = false
                                        )
                                    }
                                },
                                onFailure = {
                                    snackbarHostState.showSnackbar(
                                        message = "网络连接失败",
                                        duration = SnackbarDuration.Long,
                                        withDismissAction = false
                                    )
                                }
                            )
                        } finally {
                            // 无论成功失败，500ms 后重新启用按钮
                            delay(500)
                            isLoginButtonEnabled = true
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                ),
                // 使用新的状态控制按钮启用/禁用
                enabled = isLoginButtonEnabled && !isLoading && username.isNotBlank() && password.isNotBlank()
            ) {
                AnimatedContent(
                    targetState = isLoading,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = ""
                ) { loading ->
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            "登录",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 注册提示
            Row(
                modifier = Modifier.padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "还没有账号? ",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                TextButton(
                    onClick = { /* TODO: 跳转到注册页面 */ },
                    enabled = !isLoading
                ) {
                    Text("立即注册")
                }
            }
        }

        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            snackbar = { snackbarData ->
                Snackbar(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(8.dp),
                    shape = RoundedCornerShape(8.dp),
                    containerColor = when {
                        snackbarData.visuals.message.contains("成功") -> 
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                        else -> 
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)
                    },
                    contentColor = when {
                        snackbarData.visuals.message.contains("成功") -> 
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else -> 
                            MaterialTheme.colorScheme.onErrorContainer
                    }
                ) {
                    Text(
                        text = snackbarData.visuals.message,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }
}

