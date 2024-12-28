package com.sheep.treasuretool.data.model

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 菜单配置数据类
 */
data class MenuConfig(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val route: String,
    val enabled: Boolean = true,
    val badge: Int = 0,
    val order: Int = 0
) 