package com.example.helloworld.ui.screens

import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Audiotrack
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.helloworld.data.cache.ResourceCacheManager
import com.example.helloworld.data.model.ImageItem
import com.example.helloworld.ui.components.GlassTopBar
import com.example.helloworld.ui.viewmodel.AuthViewModel
import com.example.helloworld.utils.LogManager
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val label: String, val iconFilled: ImageVector, val iconOutlined: ImageVector) {
    object Home : Screen("home", "首页", Icons.Filled.Home, Icons.Outlined.Home)
    object Music : Screen("music", "音乐", Icons.Filled.Audiotrack, Icons.Outlined.Audiotrack)
    object Diary : Screen("notes", "日记", Icons.Filled.Book, Icons.Outlined.Book)
    object Me : Screen("me", "我的", Icons.Filled.Person, Icons.Outlined.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    cacheManager: ResourceCacheManager
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    var showLogs by remember { mutableStateOf(false) }
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    
    val navItems = listOf(
        Screen.Music,
        Screen.Diary,
        Screen.Me
    )
    
    val syncProgress by cacheManager.syncProgress.collectAsState()
    val isSyncing by cacheManager.isSyncing.collectAsState()

    LaunchedEffect(Unit) {
        cacheManager.startBackgroundSync()
    }

    if (showLogs) {
        LogViewerDialog(onDismiss = { showLogs = false })
    }

    Scaffold(
        topBar = {
            Column {
                GlassTopBar(
                    title = { 
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val route = navBackStackEntry?.destination?.route
                        val title = when {
                            route == Screen.Home.route -> "朝夕藏念"
                            route == Screen.Music.route -> "音乐"
                            route == Screen.Diary.route -> "日记"
                            route == Screen.Me.route -> "我的"
                            route == "gallery" -> "图库"
                            route?.startsWith("image_detail") == true -> "图片详情"
                            else -> "朝夕藏念"
                        }
                        Text(title, style = MaterialTheme.typography.titleLarge) 
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate("gallery") }) {
                            Icon(Icons.Filled.PhotoLibrary, contentDescription = "Gallery")
                        }
                        IconButton(onClick = { navController.navigate(Screen.Home.route) }) {
                            Icon(Icons.Outlined.Home, contentDescription = "Home")
                        }
                        IconButton(onClick = { showLogs = true }) {
                            Icon(Icons.Filled.List, contentDescription = "Logs")
                        }
                    }
                )
                if (isSyncing) {
                    LinearProgressIndicator(
                        progress = syncProgress,
                        modifier = Modifier.fillMaxWidth().height(2.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        trackColor = Color.Transparent
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                tonalElevation = 0.dp,
                modifier = Modifier.height(64.dp)
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                navItems.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        icon = {
                            Icon(
                                if (selected) screen.iconFilled else screen.iconOutlined,
                                contentDescription = screen.label,
                                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        label = { 
                            Text(
                                screen.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            ) 
                        },
                        selected = selected,
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ),
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Music.route) { MusicScreen() }
            composable(Screen.Diary.route) { NotesScreen() }
            composable("gallery") { 
                GalleryScreen(
                    onImageClick = { image ->
                        // Pass image via JSON or shared state. For simplicity, we'll use a shared state in ViewModel or just pass URL.
                        // Here we use a simple approach: navigate with encoded URL.
                        navController.navigate("image_detail/${Uri.encode(image.url)}")
                    }
                ) 
            }
            composable("image_detail/{imageUrl}") { backStackEntry ->
                val imageUrl = backStackEntry.arguments?.getString("imageUrl") ?: ""
                val galleryViewModel: com.example.helloworld.ui.viewmodel.GalleryViewModel = hiltViewModel()
                val images by galleryViewModel.images.collectAsState()
                val image = images.find { it.url == Uri.decode(imageUrl) }
                
                if (image != null) {
                    ImageDetailScreen(
                        image = image,
                        onBack = { navController.popBackStack() },
                        cacheManager = cacheManager
                    )
                }
            }
            composable(Screen.Me.route) {
                if (isLoggedIn) {
                    ProfileScreen(onLogout = { authViewModel.logout() })
                } else {
                    LoginScreen(onLoginSuccess = {
                        authViewModel.checkSession()
                    })
                }
            }
        }
    }
}

@Composable
fun LogViewerDialog(onDismiss: () -> Unit) {
    val logs by LogManager.logs.collectAsState()
    val context = LocalContext.current
    var errorMessage by remember { mutableStateOf<String?>(null) }

    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = { Text("复制失败") },
            text = { Text(errorMessage!!) },
            confirmButton = {
                TextButton(onClick = { errorMessage = null }) { Text("确定") }
            }
        )
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("App Logs") },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth().height(400.dp)) {
                items(logs) { log ->
                    Text(
                        text = "[${log.timestamp}] ${log.level} ${log.tag}: ${log.message}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val logText = logs.joinToString("\n") { "[${it.timestamp}] ${it.level} ${it.tag}: ${it.message}" }
                try {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = android.content.ClipData.newPlainText("App Logs", logText)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "已复制", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    errorMessage = e.message ?: "未知错误"
                }
            }) { Text("一键复制") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        }
    )
}
