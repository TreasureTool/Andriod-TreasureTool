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
    primary = Color(0xFF89B4FA),          // 浅蓝色
    onPrimary = Color(0xFF1E1E2E),        // 深色背景
    primaryContainer = Color(0xFF313244),  // 次要容器
    onPrimaryContainer = Color(0xFFCDD6F4),// 容器文字
    secondary = Color(0xFFF5C2E7),        // 粉色
    onSecondary = Color(0xFF1E1E2E),
    secondaryContainer = Color(0xFF313244),
    onSecondaryContainer = Color(0xFFCDD6F4),
    tertiary = Color(0xFFFAB387),         // 橙色
    onTertiary = Color(0xFF1E1E2E),
    tertiaryContainer = Color(0xFF313244),
    onTertiaryContainer = Color(0xFFCDD6F4),
    error = Color(0xFFF38BA8),            // 红色
    onError = Color(0xFF1E1E2E),
    errorContainer = Color(0xFF45475A),
    onErrorContainer = Color(0xFFF38BA8),
    background = Color(0xFF1E1E2E),       // 深色背景
    onBackground = Color(0xFFCDD6F4),     // 文字
    surface = Color(0xFF181825),          // 表面
    onSurface = Color(0xFFCDD6F4),        // 表面文字
    surfaceVariant = Color(0xFF313244),   // 变体表面
    onSurfaceVariant = Color(0xFFBAC2DE), // 变体表面文字
    outline = Color(0xFF313244),          // 轮廓 - 修改为与背景相近的颜色
    outlineVariant = Color(0xFF313244)    // 变体轮廓
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF10C3F1),          // 蓝色
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3F2FD),
    onPrimaryContainer = Color(0xFF1565C0),
    secondary = Color(0xFFE91E63),        // 粉色
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFEBEE),
    onSecondaryContainer = Color(0xFFC2185B),
    tertiary = Color(0xFFFF9800),         // 橙色
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE0B2),
    onTertiaryContainer = Color(0xFFE65100),
    error = Color(0xFFD32F2F),           // 红色
    onError = Color.White,
    errorContainer = Color(0xFFFFEBEE),
    onErrorContainer = Color(0xFFC62828),
    background = Color.White,
    onBackground = Color(0xFF121212),
    surface = Color.White,
    onSurface = Color(0xFF121212),
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF616161),
    outline = Color(0xFFE0E0E0),
    outlineVariant = Color(0xFFBDBDBD)
)

@Composable
fun TreasureToolTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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