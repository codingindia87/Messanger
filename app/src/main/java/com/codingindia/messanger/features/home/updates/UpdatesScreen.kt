package com.codingindia.messanger.features.home.updates

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.codingindia.messanger.R

// ... बाकी imports
import coil.compose.AsyncImage
import com.codingindia.messanger.features.home.updates.domain.Update

@Composable
fun UpdatesScreen(
    navController: NavController,
    viewModel: UpdatesViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val realStatuses by viewModel.allStatuses.collectAsStateWithLifecycle()
    val isUploading by viewModel.isUploading.collectAsStateWithLifecycle()
    val isFetchingUpdates by viewModel.isFetchingUpdates.collectAsStateWithLifecycle()

    val currentUid = viewModel.currentUserId
    val myStatusUpdate = realStatuses.find { it.uid == currentUid }
    val otherStatuses = realStatuses.filter { it.uid != currentUid }

    val multipleMediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                viewModel.sendStatus(uris)
            }
        }
    )

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    multipleMediaPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                    )
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Camera")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize()) {

            // Show Progress Bar for uploading
            if (isUploading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = paddingValues.calculateTopPadding()),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Updates",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )

                        if (isUploading) {
                            Text(
                                text = "Sending...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Show shimmer or actual content
                    if (isFetchingUpdates) {
                        ShimmerMyStatusItem()
                    } else {
                        MyStatusItem(
                            existingStatus = myStatusUpdate,
                            onAddClick = {
                                if (!isUploading) {
                                    multipleMediaPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                                    )
                                }
                            },
                            onViewClick = {
                                myStatusUpdate?.let { update ->
                                    navController.currentBackStackEntry?.savedStateHandle?.apply {
                                        set("status_urls", update.url)
                                        set("status_user_name", "My Status")
                                        set("status_user_image", update.userImage)
                                        set("ago_time", update.timeAgo?.let { formatTime(it) })
                                        set("id", update.id)
                                    }
                                    navController.navigate("view_status")
                                }
                            }
                        )
                    }
                }

                // Show shimmer or actual content for other statuses
                if (isFetchingUpdates) {
                    item {
                        Text(
                            text = "Recent updates",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    items(5) { // Show 5 shimmer placeholders
                        ShimmerStatusItem()
                    }
                } else {
                    if (otherStatuses.isNotEmpty()) {
                        item {
                            Text(
                                text = "Recent updates",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        items(otherStatuses) { update ->
                            StatusItem(
                                update,
                                onStatusClick = { selectedUpdate ->
                                    navController.currentBackStackEntry?.savedStateHandle?.apply {
                                        set("status_urls", selectedUpdate.url)
                                        set("status_user_name", selectedUpdate.userName)
                                        set("status_user_image", selectedUpdate.userImage)
                                        set("ago_time", selectedUpdate.timeAgo?.let { formatTime(it) })
                                        set("id", selectedUpdate.id)
                                    }
                                    navController.navigate("view_status")
                                }
                            )
                        }
                    } else {
                        item {
                            Text(
                                text = "No recent updates",
                                modifier = Modifier.padding(vertical = 20.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

// समय को सुंदर दिखाने के लिए एक छोटा हेल्पर फंक्शन
fun formatTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        else -> "${diff / 3600_000}h ago"
    }
}

@Composable
fun MyStatusItem(
    existingStatus: Update?,
    onAddClick: () -> Unit,
    onViewClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = if (existingStatus != null) onViewClick else onAddClick)
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier.size(56.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            // Logic: If status exists, show that image. If not, show default profile.
            if (existingStatus != null) {
                // Show the actual status thumbnail with a border
                AsyncImage(
                    model = existingStatus.userImage, // Or existingStatus.thumbnailUrl if you have one
                    contentDescription = "My Status",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .padding(2.dp)
                )
            } else {
                // Default Profile Image
                Image(
                    painter = painterResource(R.drawable.boy),
                    contentDescription = "My Profile",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )

                // Only show Plus icon if there is NO status
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Status",
                    tint = Color.White,
                    modifier = Modifier
                        .size(22.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
                        .padding(2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = "My Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            // Change text based on state
            Text(
                text = if (existingStatus != null) formatTime(existingStatus.timeAgo ?: System.currentTimeMillis()) else "Tap to add status update",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@Composable
fun StatusItem(status: Update, onStatusClick: (Update) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onStatusClick(status)
            }
            .padding(vertical = 8.dp)
    ) {
        // Status Ring Border (Green/Blue indicate unseen)
        Box(
            modifier = Modifier
                .size(56.dp)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .padding(4.dp) // Gap between border and image
        ) {
            AsyncImage(
                model = status.userImage,
                contentDescription = null,
                modifier = Modifier.clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = status.userName ?: "Unknow",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = formatTime(status.timeAgo ?: 12L),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

