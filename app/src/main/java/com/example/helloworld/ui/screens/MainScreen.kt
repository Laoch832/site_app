package com.example.helloworld.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.outlined.Audiotrack
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.helloworld.ui.components.GlassTopBar
import com.example.helloworld.utils.LogManager
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val label: String, val iconFilled: ImageVector, val iconOutlined: ImageVector) {
    object Home : Screen("home", "首页", Icons.Filled.Home, Icons.Outlined.Home)
    object Music : Screen("music", "音乐", Icons.Filled.Audiotrack, Icons.Outlined.Audiotrack)
    object Gallery : Screen("gallery", "画廊", Icons.Filled.PhotoLibrary, Icons.Outlined.PhotoLibrary)
    object Notes : Screen("notes", "日记", Icons.Filled.Book, Icons.Outlined.Book)
    object Login : Screen("login", "登录", Icons.Filled.Menu, Icons.Filled.Menu) // Icon placeholder
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showLogs by remember { mutableStateOf(false) }

    val items = listOf(
        Screen.Home,
        Screen.Music,
        Screen.Gallery,
        Screen.Notes,
        Screen.Login
    )

    if (showLogs) {
        LogViewerDialog(onDismiss = { showLogs = false })
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    "朝夕藏念",
                    modifier = Modifier.padding(24.dp),
                    style = MaterialTheme.typography.headlineLarge
                )
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationDrawerItem(
                        label = { Text(screen.label) },
                        selected = selected,
                        icon = {
                            Icon(
                                if (selected) screen.iconFilled else screen.iconOutlined,
                                contentDescription = screen.label
                            )
                        },
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                            scope.launch { drawerState.close() }
                        }
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                GlassTopBar(
                    title = { Text("朝夕藏念", style = MaterialTheme.typography.titleLarge) },
                    actions = {
                        IconButton(onClick = { showLogs = true }) {
                            Icon(Icons.Filled.List, contentDescription = "Logs")
                        }
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Home.route) { HomeScreen() }
                composable(Screen.Music.route) { MusicScreen() }
                composable(Screen.Gallery.route) { GalleryScreen() }
                composable(Screen.Notes.route) { NotesScreen() }
                composable(Screen.Login.route) {
                    LoginScreen(onLoginSuccess = {
                        navController.popBackStack()
                    })
                }
            }
        }
    }
}

@Composable
fun LogViewerDialog(onDismiss: () -> Unit) {
    val logs by LogManager.logs.collectAsState()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("App Logs") },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth().height(400.dp)) {
                items(logs) { log ->
                    Text(
                        text = "${log.timestamp} ${log.level} ${log.tag}: ${log.message}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
        dismissButton = {
            TextButton(onClick = { LogManager.clear() }) { Text("Clear") }
        }
    )
}
