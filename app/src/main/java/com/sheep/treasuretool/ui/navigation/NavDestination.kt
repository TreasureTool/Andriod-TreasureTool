package com.sheep.treasuretool.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed interface NavDestination {
    val route: String
    val icon: ImageVector
    val label: String

    data object Home : NavDestination {
        override val route = "home"
        override val icon = Icons.Default.Home
        override val label = "首页"
    }

    data object Chat : NavDestination {
        override val route = "chat"
        override val icon = Icons.AutoMirrored.Filled.Chat
        override val label = "消息"
    }

    data object Tools : NavDestination {
        override val route = "tools"
        override val icon = Icons.Default.Build
        override val label = "工具"
    }

    data object Profile : NavDestination {
        override val route = "profile"
        override val icon = Icons.Default.Person
        override val label = "我的"
    }

    companion object {
        val bottomNavItems = listOf(Home, Chat, Tools, Profile)
    }
} 