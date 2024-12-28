package com.sheep.treasuretool.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sheep.treasuretool.data.local.UserPreferences
import kotlinx.coroutines.flow.first
import org.koin.compose.koinInject

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier
) {
    val userPreferences: UserPreferences = koinInject()
    var nickname by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val currentUser = userPreferences.currentUser.first()
        nickname = currentUser.nickname
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "欢迎回来",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = nickname,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
} 