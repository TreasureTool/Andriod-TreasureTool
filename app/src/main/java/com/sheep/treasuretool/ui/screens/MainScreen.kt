package com.sheep.treasuretool.ui.screens

import android.app.ActivityOptions
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.sheep.treasuretool.data.local.UserPreferences
import com.sheep.treasuretool.ui.navigation.NavMenuConfig
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.sheep.treasuretool.R
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination
    val currentRoute = currentDestination?.route?.substringBefore("/")

    val menuConfigs by NavMenuConfig.menuConfigs.collectAsState()
    val userPreferences = koinInject<UserPreferences>()
    val currentUser by userPreferences.currentUser.collectAsState(initial = null)

    LaunchedEffect(currentUser) {
        NavMenuConfig.setMenuEnabled("chat", currentUser?.enabled == true)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            if (currentRoute in menuConfigs.map { it.route }) {
                TopAppBar(
                    title = { 
                        Text(
                            menuConfigs.find { it.route == currentRoute }?.title ?: "首页"
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    )
                )
            }
        },
        bottomBar = {
            if (currentRoute in menuConfigs.map { it.route }) {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 0.dp
                ) {
                    NavigationBar(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MaterialTheme.colorScheme.background,
                        tonalElevation = 0.dp,
                        windowInsets = WindowInsets(0)
                    ) {
                        menuConfigs.filter { it.enabled }.forEach { menu ->
                            val selected = currentDestination?.hierarchy?.any { it.route == menu.route } == true
                            NavigationBarItem(
                                icon = { 
                                    BadgedBox(
                                        badge = {
                                            if (menu.badge > 0) {
                                                Badge { Text(menu.badge.toString()) }
                                            }
                                        }
                                    ) {
                                        Icon(menu.icon, contentDescription = menu.title)
                                    }
                                },
                                label = { Text(menu.title) },
                                selected = selected,
                                onClick = {
                                    if (!selected) {
                                        navController.navigate(menu.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                            anim {
                                                enter = 0
                                                exit = 0
                                                popEnter = 0
                                                popExit = 0
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = menuConfigs.first().route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            builder = {
                menuConfigs.forEach { menu ->
                    composable(
                        route = menu.route,
                        enterTransition = { EnterTransition.None },
                        exitTransition = { ExitTransition.None },
                        popEnterTransition = { EnterTransition.None },
                        popExitTransition = { ExitTransition.None }
                    ) {
                        when (menu.route) {
                            "home" -> HomeScreen()
                            "chat" -> ContactListScreen(
                                onContactClick = { contact ->
                                    val intent = Intent(context, ChatActivity::class.java).apply {
                                        putExtra(
                                            ChatActivity.EXTRA_CONTACT,
                                            Json.encodeToString(contact)
                                        )
                                    }
                                    val options = ActivityOptions.makeCustomAnimation(
                                        context,
                                        R.anim.slide_in_right,
                                        R.anim.slide_out_left
                                    )
                                    context.startActivity(intent, options.toBundle())
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                            "tools" -> ToolsScreen()
                            "profile" -> ProfileScreen()
                        }
                    }
                }
            }
        )
    }
}