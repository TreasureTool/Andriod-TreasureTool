package com.sheep.treasuretool.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import com.sheep.treasuretool.data.model.MenuConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * 导航菜单配置管理
 */
object NavMenuConfig {
    // 默认菜单配置
    private val defaultMenus = listOf(
        MenuConfig(
            id = "home",
            title = "首页",
            icon = Icons.Default.Home,
            route = "home",
            order = 0
        ),
        MenuConfig(
            id = "chat",
            title = "消息",
            icon = Icons.AutoMirrored.Filled.Chat,
            route = "chat",
            order = 1
        ),
        MenuConfig(
            id = "tools",
            title = "工具",
            icon = Icons.Default.Build,
            route = "tools",
            order = 2
        ),
        MenuConfig(
            id = "profile",
            title = "我的",
            icon = Icons.Default.Person,
            route = "profile",
            order = 3
        )
    )

    // 使用 StateFlow 管理菜单配置
    private val _menuConfigs = MutableStateFlow(defaultMenus)
    val menuConfigs: StateFlow<List<MenuConfig>> = _menuConfigs

    /**
     * 启用/禁用菜单项
     */
    fun setMenuEnabled(menuId: String, enabled: Boolean) {
        val currentMenus = _menuConfigs.value.toMutableList()
        val index = currentMenus.indexOfFirst { it.id == menuId }
        if (index != -1) {
            currentMenus[index] = currentMenus[index].copy(enabled = enabled)
            _menuConfigs.value = currentMenus
        }
    }
} 