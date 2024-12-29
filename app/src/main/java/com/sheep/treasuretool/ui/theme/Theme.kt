package com.sheep.treasuretool.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import android.os.Build
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    // 主要颜色
    primary = Color(0xFFE0E1E3),          // 主要颜色（浅蓝色）
    onPrimary = Color(0xFF000000),        // 主要颜色上的文字
    primaryContainer = Color(0xFF3B3A3A),  // 主容器（深色背景）
    onPrimaryContainer = Color(0xFF07C160),// 主容器上的文字
    
    // 次要颜色
    secondary = Color(0xFFF5C2E7),        // 次要颜色（粉色）
    onSecondary = Color(0xFF1E1E2E),      // 次要颜色上的文字
    secondaryContainer = Color(0xFF313244),// 次要容器
    onSecondaryContainer = Color(0xFFCDD6F4),// 次要容器上的文字
    
    // 第三颜色
    tertiary = Color(0xFFFAB387),         // 第三颜色（橙色）
    onTertiary = Color(0xFF1E1E2E),       // 第三颜色上的文字
    tertiaryContainer = Color(0xFF313244), // 第三容器
    onTertiaryContainer = Color(0xFFCDD6F4),// 第三容器上的文字
    
    // 错误颜色
    error = Color(0xFFF38BA8),            // 错误颜色（红色）
    onError = Color(0xFF1E1E2E),          // 错误颜色上的文字
    errorContainer = Color(0xFF45475A),    // 错误容器
    onErrorContainer = Color(0xFFF38BA8),  // 错误容器上的文字
    
    // 背景和表面
    background = Color(0xFF000000),       // 背景色（深色）
    onBackground = Color(0xFFFFFFFF),     // 背景上的文字
    surface = Color(0xFF000000),          // 表面颜色（深色）
    onSurface = Color(0xFFFFFFFF),        // 表面上的文字
    surfaceVariant = Color(0xFF313244),   // 表面变体（稍浅的深色）
    onSurfaceVariant = Color(0xFFBAC2DE), // 表面变体上的文字
    
    // 轮廓
    outline = Color(0xFF45475A),          // 轮廓颜色
    outlineVariant = Color(0xFF313244)    // 轮廓变体
)

private val LightColorScheme = lightColorScheme(
    // 主要颜色
    primary = Color(0xFF1A73E8),          // 主要颜色（Google蓝）
    onPrimary = Color.White,              // 主要颜色上的文字
    primaryContainer = Color(0xFFE8F0FE),  // 主容器（浅蓝背景）
    onPrimaryContainer = Color(0xFF1A73E8),// 主容器上的文字
    
    // 次要颜色
    secondary = Color(0xFFE91E63),        // 次要颜色（粉色）
    onSecondary = Color.White,            // 次要颜色上的文字
    secondaryContainer = Color(0xFFFFEBEE),// 次要容器
    onSecondaryContainer = Color(0xFFC2185B),// 次要容器上的文字
    
    // 第三颜色
    tertiary = Color(0xFFFF9800),         // 第三颜色（橙色）
    onTertiary = Color.White,             // 第三颜色上的文字
    tertiaryContainer = Color(0xFFFFECB3), // 第三容器
    onTertiaryContainer = Color(0xFFE65100),// 第三容器上的文字
    
    // 错误颜色
    error = Color(0xFFD32F2F),           // 错误颜色（红色）
    onError = Color.White,               // 错误颜色上的文字
    errorContainer = Color(0xFFFFEBEE),  // 错误容器
    onErrorContainer = Color(0xFFC62828), // 错误容器上的文字
    
    // 背景和表面
    background = Color.White,            // 背景色
    onBackground = Color(0xFF121212),    // 背景上的文字
    surface = Color.White,               // 表面颜色
    onSurface = Color(0xFF121212),       // 表面上的文字
    surfaceVariant = Color(0xFFF5F5F5),  // 表面变体（浅灰色）
    onSurfaceVariant = Color(0xFF616161),// 表面变体上的文字
    
    // 轮廓
    outline = Color(0xFFE0E0E0),         // 轮廓颜色
    outlineVariant = Color(0xFFBDBDBD)   // 轮廓变体
)

@Composable
fun TreasureToolTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}