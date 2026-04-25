package com.codingindia.messanger.features.home

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.codingindia.messanger.features.home.chats.ChatsScreen
import com.codingindia.messanger.features.home.profile.ProfileScreen
import com.codingindia.messanger.features.home.reels.ReelsScreen
import com.codingindia.messanger.features.home.updates.UpdatesScreen


@Composable
fun HomeScreen(
    rootNavController: NavController
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Permission Logic
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(), onResult = { /* Handle result */ })

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(), bottomBar = {
        // हमने जो मॉडर्न नेविगेशन बार बनाया था उसे यहाँ कॉल करेंगे
        AppBottomNavigationBar(navController = navController)
    }, floatingActionButton = {
        // सिर्फ Chats स्क्रीन पर ही FAB दिखाएं (Modern UX)
        if (currentRoute == BottomNavItem.Chats.route) {
            ExtendedFloatingActionButton(
                onClick = { rootNavController.navigate("add-chat") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
                Text(text = "New Chat", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }, floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        // content area with smooth transitions
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding()) // Bottom Bar के लिए जगह छोड़ना
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    )
                )
        ) {
            NavHost(
                navController = navController,
                startDestination = BottomNavItem.Chats.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(
                    route = BottomNavItem.Chats.route,
                    enterTransition = { fadeIn(animationSpec = tween(400)) },
                    exitTransition = { fadeOut(animationSpec = tween(400)) }) {
                    ChatsScreen(rootNavController)
                }
                composable(BottomNavItem.Updates.route) {
                    UpdatesScreen(navController = rootNavController)
                }
                composable(BottomNavItem.Profile.route) {
                    ProfileScreen(navController = rootNavController)
                }
                composable(BottomNavItem.Reels.route) {
                    ReelsScreen()
                }
            }
        }
    }
}

@Composable
fun AppBottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem.Chats, BottomNavItem.Reels, BottomNavItem.Updates, BottomNavItem.Profile
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Floating Design के लिए Surface का उपयोग
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp, vertical = 12.dp
            ) // इसे हवा में तैरता (floating) दिखाने के लिए
            .navigationBarsPadding(), // ताकी सिस्टम नेविगेशन के ऊपर रहे
        shape = RoundedCornerShape(24.dp), // मॉडर्न कर्व्ड शेप
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f), // हल्का ट्रांसपेरेंट
        tonalElevation = 8.dp, shadowElevation = 12.dp
    ) {
        NavigationBar(
            containerColor = Color.Transparent, // Surface का कलर इस्तेमाल होगा
            modifier = Modifier.height(70.dp),
            windowInsets = WindowInsets(0.dp) // एक्स्ट्रा स्पेस हटाने के लिए
        ) {
            items.forEach { item ->
                val isSelected = currentRoute == item.route

                NavigationBarItem(
                    selected = isSelected, onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }, icon = {
                    // Icon Animation या Badge लगा सकते हैं
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }, label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }, alwaysShowLabel = false, // सिर्फ सिलेक्टेड होने पर लेबल दिखाएगा (मॉडर्न लुक)
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    )
                )
            }
        }
    }
}
