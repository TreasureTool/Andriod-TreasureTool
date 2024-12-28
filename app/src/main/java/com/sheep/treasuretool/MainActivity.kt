package com.sheep.treasuretool

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.sheep.treasuretool.ui.screens.LoginScreen
import com.sheep.treasuretool.ui.theme.TreasureToolTheme
import com.sheep.treasuretool.data.local.UserPreferences
import com.sheep.treasuretool.ui.screens.SplashScreen
import com.sheep.treasuretool.ui.screens.MainScreen
import com.sheep.treasuretool.service.WebSocketService
import com.sheep.treasuretool.data.local.AvatarCache
import com.sheep.treasuretool.data.repository.UserRepository
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val userPreferences: UserPreferences by inject()
    private val avatarCache: AvatarCache by inject()
    private val userRepository: UserRepository by inject()
    private val mainScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startForegroundServiceSafely()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 设置沉浸式状态栏和导航栏
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            TreasureToolTheme {
                var isLoggedIn by remember { mutableStateOf<Boolean?>(null) }
                var hasUpdatedUserInfo by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    userPreferences.isUserLoggedIn.collect { loggedIn ->
                        if (loggedIn && !hasUpdatedUserInfo) {
                            mainScope.launch {
                                try {
                                    // 先使用本地数据预加载用户头像
                                    avatarCache.preloadAvatars()
                                    
                                    // 再异步更新用户信息
                                    launch(Dispatchers.IO) {
                                        userRepository.updateUserInfo()
                                    }
                                    // 再异步更新联系人列表
                                    launch(Dispatchers.IO) {
                                        userRepository.updateUserContact()
                                    }

                                    hasUpdatedUserInfo = true
                                } catch (e: Exception) {
                                    Log.e("MainActivity", "加载用户数据失败", e)
                                }
                            }
                        }

                        if (!loggedIn) {
                            hasUpdatedUserInfo = false
                        }

                        isLoggedIn = loggedIn

                        if (loggedIn) {
                            startWebSocketService()
                        } else {
                            stopWebSocketService()
                        }
                    }
                }
                
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding()  // 只保留状态栏的内边距
                        .navigationBarsPadding(),  // 移除导航栏的内边距
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (isLoggedIn) {
                        null -> {
                            SplashScreen()
                        }
                        false -> {
                            LoginScreen()
                        }
                        true -> {
                            MainScreen()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mainScope.cancel()
    }

    private fun startWebSocketService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    startForegroundServiceSafely()
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            startForegroundServiceSafely()
        }
    }

    private fun startForegroundServiceSafely() {
        try {
            Intent(this, WebSocketService::class.java).also { intent ->
                startForegroundService(intent)
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "启动服务失败", e)
        }
    }

    private fun stopWebSocketService() {
        Intent(this, WebSocketService::class.java).also { intent ->
            stopService(intent)
        }
    }
}