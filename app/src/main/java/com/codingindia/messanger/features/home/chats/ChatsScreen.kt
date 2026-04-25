package com.codingindia.messanger.features.home.chats

import android.util.Base64
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.codingindia.messanger.R
import com.codingindia.messanger.features.home.domain.Conversation
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(navController: NavController, viewModel: ChatsViewModel = hiltViewModel()) {
    val conversations by viewModel.conversations.collectAsState()

    var selectedIds by remember { mutableStateOf(setOf<String>()) }
    val isSelectionMode = selectedIds.isNotEmpty()
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Default.DeleteForever, contentDescription = null) },
            title = { Text("Delete Conversations") },
            text = { Text("Permanently delete ${selectedIds.size} selected chats?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteConversations(selectedIds)
                        selectedIds = emptySet()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            })
    }

    Scaffold(topBar = {
        // Animated transition between Normal and Selection TopBar
        AnimatedVisibility(
            visible = isSelectionMode, enter = fadeIn(), exit = fadeOut()
        ) {
            SelectionTopBar(
                selectedCount = selectedIds.size,
                onClearSelection = { selectedIds = emptySet() },
                onDelete = { showDeleteDialog = true })
        }
        AnimatedVisibility(
            visible = !isSelectionMode, enter = fadeIn(), exit = fadeOut()
        ) {
            CenterAlignedTopAppBar(
                title = {
                Text(
                    "Messenger", style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }, actions = {
                IconButton(onClick = { navController.navigate("camera") }) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = "Camera")
                }
            }, colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
            )
        }
    }) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            items(items = conversations, key = { it.user.id }) { conversation ->
                val isSelected = selectedIds.contains(conversation.user.id)
                ConversationItem(
                    navController = navController,
                    conversation = conversation,
                    isSelected = isSelected,
                    onClick = {
                        if (isSelectionMode) {
                            selectedIds = if (isSelected) selectedIds - conversation.user.id
                            else selectedIds + conversation.user.id
                        } else {
                            navController.navigate("message/${conversation.user.id}")
                        }
                    },
                    onLongClick = {
                        if (!isSelectionMode) {
                            selectedIds = selectedIds + conversation.user.id
                        }
                    },
                    unReadMessageCount = viewModel.getUnReadMessageCount(conversation.user.id)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ConversationItem(
    navController: NavController,
    conversation: Conversation,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    unReadMessageCount: Int
) {
    // Animate background color change
    val backgroundColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        else MaterialTheme.colorScheme.surface, label = "bgColor"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .combinedClickable(
                onClick = onClick, onLongClick = onLongClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar with Selection Indicator Overlay
        Box(contentAlignment = Alignment.BottomEnd) {
            AsyncImage(
                model = conversation.user.imageUrl,
                placeholder = painterResource(R.drawable.boy),
                contentDescription = null,
                error = painterResource(id = R.drawable.boy),
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .clickable {
                        val encodedUrl = encodeUrlToBase64(conversation.user.imageUrl ?: "")
                        navController.navigate("fullScreenImage/$encodedUrl")
                    },
                contentScale = ContentScale.Crop,
            )

            // Selection Checkmark Overlay
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Content Column
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = conversation.user.name ?: "Unknown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Timestamp aligned to top-right
                Text(
                    text = getSmartTimestamp(conversation.lastMessage.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (unReadMessageCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Sent Status Icon (only if I sent the last message)
                if (conversation.lastMessage.senderId == Firebase.auth.uid) {
                    val (icon, tint) = when (conversation.lastMessage.status) {
                        "sending" -> Icons.Default.AccessTime to Color.Gray // WhatsApp Blue
                        "send" -> Icons.Default.DoneAll to Color.Gray
                        "failed" -> Icons.Default.ErrorOutline to Color.Gray
                        else -> Icons.Default.DoneAll to Color(0xFF34B7F1)
                    }
                    if (conversation.lastMessage.isRead) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            tint = Color(0xFF34B7F1),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                    } else {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = tint
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }

                // Last Message Content
                val messageText = if (conversation.lastMessage.messageType == "image") {
                    "📷 Photo"
                } else {
                    conversation.lastMessage.messageContent ?: ""
                }

                Text(
                    text = messageText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Unread Badge (if count > 0)
                if (unReadMessageCount > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = if (unReadMessageCount >= 9) "9+" else unReadMessageCount.toString(),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionTopBar(selectedCount: Int, onClearSelection: () -> Unit, onDelete: () -> Unit) {
    TopAppBar(
        title = {
        Text(
            "$selectedCount",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
    }, navigationIcon = {
        IconButton(onClick = onClearSelection) {
            Icon(Icons.Default.Close, contentDescription = "Close Selection")
        }
    }, actions = {
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete Selected")
        }
    }, colors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    )
    )
}

// Helper to format smarter timestamps (e.g. "10:30 AM", "Yesterday", "Monday")
fun getSmartTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val date = Date(timestamp)

    return when {
        diff < 24 * 60 * 60 * 1000 -> SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date)
        diff < 48 * 60 * 60 * 1000 -> "Yesterday"
        else -> SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(date)
    }
}

fun encodeUrlToBase64(url: String): String {
    return Base64.encodeToString(url.toByteArray(), Base64.URL_SAFE or Base64.NO_WRAP)
}
