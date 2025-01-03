package com.sheep.treasuretool.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.sheep.treasuretool.data.model.Contact
import kotlinx.serialization.json.Json
import com.sheep.treasuretool.R
import com.sheep.treasuretool.ui.theme.TreasureToolTheme
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.runtime.DisposableEffect

class ChatActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 设置沉浸式状态栏和导航栏
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val contactJson = intent.getStringExtra(EXTRA_CONTACT)
        val contact = contactJson?.let { Json.decodeFromString<Contact>(it) }

        if (contact == null) {
            finish()
            return
        }

        setContent {
            TreasureToolTheme {
                val focusManager = LocalFocusManager.current
                
                DisposableEffect(Unit) {
                    onDispose {
                        focusManager.clearFocus()
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding()
                        .navigationBarsPadding(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChatScreen(
                        contact = contact,
                        onBackClick = { 
                            focusManager.clearFocus()
                            finish() 
                        }
                    )
                }
            }
        }
    }

    override fun finish() {
        super.finish()
        // 设置退出动画
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    companion object {
        const val EXTRA_CONTACT = "contact"
    }
} 