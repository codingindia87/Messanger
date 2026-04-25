package com.codingindia.messanger.features.home.updates

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.codingindia.messanger.R
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusViewerScreen(
    urls: List<String>,
    userName: String = "My Status",
    userImage: String? = "",
    onBack: () -> Unit,
    timeAgo: String = "",
    id: String = "",
    viewModel: UpdatesViewModel = hiltViewModel()
) {
    if (urls.isEmpty()) {
        onBack()
        return
    }

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val sheetState = rememberModalBottomSheetState()
    var showViewsSheet by remember { mutableStateOf(false) }

    // Collect Real-time Views List
    val viewersList by viewModel.viewersList.collectAsState()

    // --- 1. View Logic Initialization ---
    LaunchedEffect(key1 = id) {
        if (id.isNotEmpty()) {
            if (userName == "My Status") {
                // If it's MY status -> Start fetching the list of viewers immediately
                // This ensures the count (viewersList.size) is correct before clicking
                viewModel.getViews(id)
            } else {
                // If it's SOMEONE ELSE'S status -> Mark it as viewed by me
                viewModel.updateViews(id)
            }
        }
    }

    val statusUrls = remember { mutableStateListOf<String>().apply { addAll(urls) } }

    LaunchedEffect(statusUrls.size) {
        if (statusUrls.isEmpty()) {
            onBack()
        }
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var isPaused by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var replyText by remember { mutableStateOf("") }
    var isReplyFocused by remember { mutableStateOf(false) }

    if (statusUrls.isNotEmpty()) {
        if (currentIndex >= statusUrls.size) {
            currentIndex = statusUrls.size - 1
        }
    } else {
        return
    }

    val currentUrl = statusUrls[currentIndex]
    val isVideo = remember(currentUrl) {
        currentUrl.contains(".mp4", true) || currentUrl.contains(
            ".mkv",
            true
        ) || currentUrl.contains("video", true)
    }
    var currentProgress by remember(currentIndex) { mutableFloatStateOf(0f) }

    fun nextStory() {
        if (currentIndex < statusUrls.size - 1) {
            currentIndex++
        } else {
            onBack()
        }
    }

    fun previousStory() {
        if (currentIndex > 0) {
            currentIndex--
        }
    }

    val shouldPause = isPaused || showViewsSheet || showDeleteDialog || isReplyFocused

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(onPress = {
                    if (!isReplyFocused && !showViewsSheet) {
                        isPaused = true
                        tryAwaitRelease()
                        if (!showDeleteDialog) isPaused = false
                    }
                }, onTap = { offset ->
                    if (isReplyFocused) {
                        focusManager.clearFocus()
                        isReplyFocused = false
                        isPaused = false
                    } else {
                        val screenWidthPx = size.width
                        if (offset.x < screenWidthPx * 0.2f) {
                            previousStory()
                        } else {
                            nextStory()
                        }
                    }
                })
            }) {
        // --- Media Content ---
        key(currentUrl) {
            if (isVideo) {
                StatusVideoPlayer(
                    url = currentUrl,
                    isPaused = shouldPause,
                    onProgressUpdate = { current, total ->
                        if (total > 0) {
                            currentProgress = current.toFloat() / total.toFloat()
                        }
                    },
                    onComplete = { nextStory() })
            } else {
                StatusImage(
                    url = currentUrl,
                    isPaused = shouldPause,
                    onProgressUpdate = { progress -> currentProgress = progress },
                    onComplete = { nextStory() })
            }
        }

        // --- Top Header ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)
                    )
                )
                .statusBarsPadding()
                .padding(top = 12.dp, start = 8.dp, end = 8.dp, bottom = 32.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    statusUrls.forEachIndexed { index, _ ->
                        val progress = when {
                            index < currentIndex -> 1f
                            index == currentIndex -> currentProgress
                            else -> 0f
                        }
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .weight(1f)
                                .height(2.5.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.3f),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    AsyncImage(
                        model = userImage,
                        contentDescription = null,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.Gray),
                        contentScale = ContentScale.Crop,
                        error = painterResource(id = R.drawable.boy)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            userName,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            timeAgo,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        )
                    }
                    if (userName == "My Status") {
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(
                            onClick = {
                                isPaused = true
                                showDeleteDialog = true
                            }) {
                            Icon(Icons.Default.Delete, "Delete", tint = Color.White)
                        }
                    }
                }
            }
        }

        // --- Bottom Section (Reply OR Views) ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .navigationBarsPadding()
                .padding(bottom = 16.dp), contentAlignment = Alignment.BottomCenter
        ) {
            if (userName != "My Status") {
                // Reply UI for others' status
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextField(
                        value = replyText,
                        onValueChange = {
                            replyText = it
                            isPaused = true
                        },
                        placeholder = { Text("Reply...", color = Color.White.copy(alpha = 0.7f)) },
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .onFocusChanged { focusState ->
                                isReplyFocused = focusState.isFocused
                                isPaused = focusState.isFocused
                            },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Black.copy(alpha = 0.5f),
                            unfocusedContainerColor = Color.Black.copy(alpha = 0.5f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                    val isTyping = replyText.isNotBlank()
                    IconButton(
                        onClick = {
                            if (isTyping) {
                                Toast.makeText(
                                    context,
                                    "Reply sent: $replyText",
                                    Toast.LENGTH_SHORT
                                ).show()
                                replyText = ""
                                focusManager.clearFocus()
                                isPaused = false
                            } else {
                                Toast.makeText(context, "Liked ❤️", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isTyping) Icons.Default.Send else Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            } else {
                // --- VIEWS INDICATOR (Only for My Status) ---
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = {
                            isPaused = true
                            showViewsSheet = true
                            // Already fetching, but good to ensure latest state on open
                            viewModel.getViews(id)
                        }) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Views",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Text(
                        text = "${viewersList.size}", // Updated automatically by LaunchedEffect
                        color = Color.White, style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }

    // --- Views Bottom Sheet ---
    if (showViewsSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showViewsSheet = false
                isPaused = false
            }, sheetState = sheetState, containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .heightIn(min = 200.dp, max = 500.dp)
            ) {
                Text(
                    text = "Viewed by ${viewersList.size}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (viewersList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No views yet", color = Color.Gray)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(viewersList) { user ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = user.imageUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color.LightGray),
                                    contentScale = ContentScale.Crop,
                                    error = painterResource(id = R.drawable.boy)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = user.name ?: "Unknown",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // --- Delete Dialog ---
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false; isPaused = false },
            title = { Text("Delete Status?") },
            text = { Text("Are you sure you want to delete this status update?") },
            confirmButton = {
                Button(
                    onClick = {
                        val urlToDelete = statusUrls[currentIndex]
                        statusUrls.removeAt(currentIndex)
                        val updatedList = statusUrls.toList()
                        viewModel.deleteStatus(id, urlToDelete, updatedList)
                        Toast.makeText(context, "Status Deleted", Toast.LENGTH_SHORT).show()
                        if (statusUrls.isEmpty()) {
                            onBack()
                        } else {
                            if (currentIndex >= statusUrls.size) currentIndex = statusUrls.size - 1
                            currentProgress = 0f
                        }
                        showDeleteDialog = false
                        isPaused = false
                    }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Delete", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false; isPaused = false
                }) { Text("Cancel") }
            })
    }
}

// ... StatusImage and StatusVideoPlayer remain unchanged ...
@Composable
fun StatusImage(
    url: String,
    isPaused: Boolean,
    onProgressUpdate: (Float) -> Unit,
    onComplete: () -> Unit
) {
    val duration = 5000L
    var isImageLoaded by remember { mutableStateOf(false) }
    var elapsedTime by remember { mutableLongStateOf(0L) }
    var lastTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AsyncImage(
            model = url,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            onSuccess = { isImageLoaded = true; lastTime = System.currentTimeMillis() })
        if (!isImageLoaded) CircularProgressIndicator(color = Color.White)
    }

    LaunchedEffect(url, isImageLoaded, isPaused) {
        if (isImageLoaded) {
            lastTime = System.currentTimeMillis()
            while (true) {
                val currentTime = System.currentTimeMillis()
                if (!isPaused) {
                    val delta = currentTime - lastTime
                    elapsedTime += delta
                }
                lastTime = currentTime
                val progress = (elapsedTime.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
                onProgressUpdate(progress)
                if (elapsedTime >= duration) {
                    onComplete(); break
                }
                delay(50)
            }
        } else {
            lastTime = System.currentTimeMillis()
        }
    }
}

@Composable
fun StatusVideoPlayer(
    url: String,
    isPaused: Boolean,
    onProgressUpdate: (Long, Long) -> Unit,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    var isBuffering by remember { mutableStateOf(true) }
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            prepare()
            playWhenReady = true
        }
    }

    LaunchedEffect(isPaused) {
        if (isPaused) exoPlayer.pause() else if (exoPlayer.playbackState != Player.STATE_ENDED) exoPlayer.play()
    }

    LaunchedEffect(exoPlayer) {
        while (true) {
            if (exoPlayer.isPlaying) onProgressUpdate(exoPlayer.currentPosition, exoPlayer.duration)
            delay(50)
        }
    }

    DisposableEffect(Unit) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> isBuffering = true
                    Player.STATE_READY -> isBuffering = false
                    Player.STATE_ENDED -> onComplete()
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose { exoPlayer.removeListener(listener); exoPlayer.release() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer; useController = false; resizeMode =
                AspectRatioFrameLayout.RESIZE_MODE_FIT
            }
        }, modifier = Modifier.fillMaxSize())
        if (isBuffering) CircularProgressIndicator(color = Color.White)
    }
}
