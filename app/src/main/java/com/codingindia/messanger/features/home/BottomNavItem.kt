package com.codingindia.messanger.features.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Update
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Chats : BottomNavItem("chats", Icons.Default.ChatBubbleOutline, "Chats")
    object Reels : BottomNavItem("reels", Icons.Default.OndemandVideo, "Reels")
    object Updates : BottomNavItem("updates", Icons.Default.Update, "Updates")
    object Profile : BottomNavItem("profile", Icons.Default.Person, "Profile")
}
