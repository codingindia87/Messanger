package com.codingindia.messanger.features.message

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.compose.ui.window.Popup
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.codingindia.messanger.R
import com.codingindia.messanger.core.utils.AudioRecorder
import com.codingindia.messanger.core.utils.Conts.formatLastSeen
import com.codingindia.messanger.core.utils.LinkText
import com.codingindia.messanger.features.fullscreenimage.SharedViewModel
import com.codingindia.messanger.features.home.domain.User
import com.codingindia.messanger.features.message.domain.Messages
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.ceil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageScreen(
    navController: NavController, viewModel: MessageViewModel = hiltViewModel(), uid: String
) {
    val user by viewModel.user.collectAsState()
    val messages: LazyPagingItems<Messages> =
        viewModel.getMessages(Firebase.auth.uid!!, uid).collectAsLazyPagingItems()

    val preferenceManager = viewModel.preferenceManager
    var repliedMessage by remember { mutableStateOf<Messages?>(null) }
    val listState = rememberLazyListState()

    // Lifecycle handling
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, preferenceManager, uid) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    preferenceManager.setActiveChatUserId(uid)
                    viewModel.updateChatRoom(uid)
                }

                Lifecycle.Event.ON_PAUSE -> {
                    preferenceManager.setActiveChatUserId(null)
                    viewModel.updateChatRoom(null)
                }

                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            preferenceManager.setActiveChatUserId(null)
        }
    }

    // Auto-scroll logic
    LaunchedEffect(key1 = messages, key2 = listState) {
        snapshotFlow { messages.itemCount }.filter { it > 0 }.collect {
            if (listState.firstVisibleItemIndex <= 3) {
                listState.animateScrollToItem(index = 0)
            }
        }
    }

    Scaffold(
        topBar = {
        user?.let { ConversationTopAppBar(navController, it) }
    },
        bottomBar = {
            ModernChatInputBar(
                navController = navController,
                viewModel = viewModel,
                user = user,
                modifier = Modifier
                    .navigationBarsPadding()
                    .imePadding(),
                repliedMessage = repliedMessage,
                onReplyDismiss = { repliedMessage = null })
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.surface // Clean background
    ) { innerPadding ->

        // Background pattern or color can go here
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                reverseLayout = true,
                state = listState,
                contentPadding = PaddingValues(bottom = 16.dp) // Extra padding for floating bar
            ) {

                // Typing Indicator
                user?.let { u ->
                    if (u.chatRoom == Firebase.auth.currentUser?.uid && u.typing == true) {
                        item(key = "typing_indicator") {
                            TypingBubble()
                        }
                    }
                }

                items(
                    count = messages.itemCount,
                    key = { index -> messages[index]?.id ?: index }) { index ->
                    val message = messages[index]
                    if (message != null) {
                        val nextMessage =
                            if (index + 1 < messages.itemCount) messages[index + 1] else null
                        val showHeader = nextMessage == null || !isSameDay(
                            message.timestamp, nextMessage.timestamp
                        )

                        if (showHeader) {
                            ModernDateHeader(timestamp = message.timestamp)
                        }

                        user?.let {
                            ModernMessageBubble(
                                message = message, onSwipeToReply = { swipedMessage ->
                                    repliedMessage = swipedMessage
                                }, navController = navController, viewModel = viewModel, user = it
                            )
                        }
                    }
                }
            }
            val scope = rememberCoroutineScope()
            val showScrollToBottom by remember {
                derivedStateOf { listState.firstVisibleItemIndex > 3 }
            }

            AnimatedVisibility(
                visible = showScrollToBottom,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(innerPadding)
            ) {
                SmallFloatingActionButton(
                    onClick = {
                        scope.launch {
                            listState.animateScrollToItem(0)
                        }
                    },
                    modifier = Modifier.size(40.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "Scroll to bottom",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ModernMessageBubble(
    message: Messages,
    onSwipeToReply: (Messages) -> Unit,
    navController: NavController,
    viewModel: MessageViewModel,
    user: User
) {
    val scope = rememberCoroutineScope()
    val swipeOffset = remember { Animatable(0f) }
    val isMe = Firebase.auth.uid == message.senderId
    var showReactionPicker by remember { mutableStateOf<Messages?>(null) }

    val myGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF007EF4), Color(0xFF2A75BC))
    )

    val otherColor = if (isSystemInDarkTheme()) Color(0xFF333333) else Color(0xFFF2F4F7)

    val textColor = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface

    val bubbleShape = if (isMe) {
        RoundedCornerShape(topStart = 20.dp, topEnd = 4.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
    }

    val arrangement = if (isMe) Arrangement.End else Arrangement.Start
    val alignment = if (isMe) Alignment.End else Alignment.Start

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (swipeOffset.value > 100f) {
                            onSwipeToReply(message)
                        }
                        scope.launch {
                            swipeOffset.animateTo(
                                0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                            )
                        }
                    }) { change, dragAmount ->
                    change.consume()
                    scope.launch {
                        val newOffset = (swipeOffset.value + dragAmount * 0.5f).coerceIn(0f, 150f)
                        swipeOffset.snapTo(newOffset)
                    }
                }
            }
            .combinedClickable(onClick = { /* Actions */ }, onLongClick = {
                showReactionPicker = message
            })
    ) {
        // Reply Icon (Hidden behind)
        Icon(
            Icons.Default.Reply,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
                .size(24.dp)
                .graphicsLayer {
                    scaleX = (swipeOffset.value / 100f).coerceIn(0f, 1f)
                    scaleY = (swipeOffset.value / 100f).coerceIn(0f, 1f)
                    alpha = (swipeOffset.value / 80f).coerceIn(0f, 1f)
                })

        // Bubble Content
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { translationX = swipeOffset.value },
            horizontalArrangement = arrangement
        ) {
            Column(
                horizontalAlignment = alignment, modifier = Modifier.padding(horizontal = 12.dp)
            ) {

                Box(
                    modifier = Modifier
                        .shadow(
                            elevation = 1.dp, shape = bubbleShape
                        ) // Subtle shadow
                        .clip(bubbleShape)
                        .then(
                            if (isMe) Modifier.background(myGradient)
                            else Modifier.background(otherColor)
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Column(horizontalAlignment = alignment) {
                        // Reply Preview
                        if (!message.replyMessage.isNullOrEmpty()) {
                            ModernReplyPreviewMessage(
                                message = message.replyMessage, isMe = isMe
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // For Audio
                        if (message.messageType == "audio") {
                            // चेक करें कि क्या फाइल लोकल स्टोरेज में मौजूद है
                            val isFileAvailable = message.localFilePaths.isNullOrEmpty()

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .width(220.dp) // थोड़ा स्पेस बढ़ाने के लिए
                            ) {
                                // Play/Pause या Download बटन logic
                                val isPlaying = false // इसे अपनी ViewModel स्टेट से जोड़ें

                                Box(
                                    contentAlignment = Alignment.Center, // यह कंटेंट को सेंटर में रखेगा
                                    modifier = Modifier.size(45.dp)
                                ){
                                    IconButton(
                                        onClick = {
                                            if (isFileAvailable) {
                                                message.localFilePaths?.let { viewModel.playAudio(it[0]) }
                                            } else {
                                                // यहाँ फाइल डाउनलोड करने का फंक्शन कॉल करें
                                                message.urls?.let { viewModel.downloadAudio(message) }
                                            }
                                        },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(textColor.copy(alpha = 0.1f), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = when {
                                                !isFileAvailable -> Icons.Default.Download  // फाइल नहीं है तो डाउनलोड
                                                isPlaying -> Icons.Default.Pause           // चल रहा है तो पॉज़
                                                else -> Icons.Default.PlayArrow            // मौजूद है तो प्ले
                                            },
                                            contentDescription = null,
                                            tint = textColor
                                        )
                                    }
                                    if (message.isDownloading){
                                        CircularProgressIndicator(
                                            progress = { message.progress / 100f },
                                            modifier = Modifier.fillMaxSize(), // Box का पूरा साइज़ लेगा
                                            color = Color.White,
                                            strokeWidth = 4.dp,
                                            trackColor = Color.White.copy(alpha = 0.2f) // हल्का बैकग्राउंड ट्रैक (Optional)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Progress Bar या Download Text Area
                                Column(modifier = Modifier.weight(1f)) {
                                    if (isFileAvailable) {
                                        // फाइल होने पर प्रोग्रेस बार दिखाएं
                                        LinearProgressIndicator(
                                            progress = { 0f }, // यहाँ currentPosition / totalDuration डालें
                                            modifier = Modifier.fillMaxWidth().height(4.dp),
                                            color = textColor,
                                            trackColor = textColor.copy(alpha = 0.2f),
                                            strokeCap = StrokeCap.Round
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = "0:00", // यहाँ ड्यूरेशन दिखाएं
                                            fontSize = 10.sp,
                                            color = textColor.copy(alpha = 0.7f)
                                        )
                                    } else {
                                        // फाइल नहीं होने पर प्रोग्रेस बार की जगह ये दिखेगा
                                        Text(
                                            text = "Tap to download audio",
                                            fontSize = 12.sp,
                                            fontStyle = FontStyle.Italic,
                                            color = textColor.copy(alpha = 0.6f),
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                        // एक खाली या डॉटेड लाइन भी दिखा सकते हैं ताकि खाली न लगे
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(2.dp)
                                                .background(textColor.copy(alpha = 0.1f))
                                        )
                                    }
                                }
                            }
                        }

                        // Images
                        if (message.messageType == "image") {
                            val sharedViewModel: SharedViewModel = hiltViewModel()
                            val isFileAvailable = !message.localFilePaths.isNullOrEmpty()
                            val images =
                                if (isFileAvailable) message.localFilePaths!! else (message.urls
                                    ?: emptyList())

                            // Corner logic for images inside bubble
                            Box(modifier = Modifier.clip(RoundedCornerShape(12.dp))) {
                                if (isFileAvailable) {
                                    Box(contentAlignment = Alignment.Center) {
                                        ImageGrid(
                                            imageUris = images,
                                            isNetworkImage = false,
                                            onImageClick = { index ->
                                                sharedViewModel.setImagesForSlider(images)
                                                navController.navigate("imageSlider/$index")
                                            })
                                        if (message.isUploading) {
                                            if (message.progress == 100) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(45.dp),
                                                    color = Color.White,
                                                    strokeWidth = 4.dp
                                                )
                                            } else {
                                                CircularProgressIndicator(
                                                    progress = { message.progress / 100f },
                                                    modifier = Modifier.size(45.dp), // Box का पूरा साइज़ लेगा
                                                    color = Color.White,
                                                    strokeWidth = 4.dp,
                                                    trackColor = Color.White.copy(alpha = 0.2f) // हल्का बैकग्राउंड ट्रैक (Optional)
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    // Blurred Preview Logic
                                    Box(contentAlignment = Alignment.Center) {
                                        ImageGrid(
                                            imageUris = images,
                                            isNetworkImage = true,
                                            modifier = Modifier.blur(20.dp), // Privacy blur
                                            onImageClick = {})
                                        // Download Button
                                        if (message.isDownloading) {
                                            Box(
                                                contentAlignment = Alignment.Center, // यह कंटेंट को सेंटर में रखेगा
                                                modifier = Modifier.size(45.dp)
                                            ) {

                                                CircularProgressIndicator(
                                                    progress = { message.progress / 100f },
                                                    modifier = Modifier.fillMaxSize(), // Box का पूरा साइज़ लेगा
                                                    color = Color.White,
                                                    strokeWidth = 4.dp,
                                                    trackColor = Color.White.copy(alpha = 0.2f) // हल्का बैकग्राउंड ट्रैक (Optional)
                                                )

                                                Text(
                                                    text = "${message.progress}%",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = Color.White,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                )
                                            }
                                        } else {
                                            FilledIconButton(
                                                onClick = { viewModel.downloadImage(message) },
                                                colors = IconButtonDefaults.filledIconButtonColors(
                                                    containerColor = Color.Black.copy(alpha = 0.5f)
                                                )
                                            ) {
                                                Icon(
                                                    Icons.Default.Download, null, tint = Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        // Text
                        if (!message.messageContent.isNullOrEmpty()) {
                            LinkText(
                                text = message.messageContent,
                                color = textColor,
                                fontSize = 16.sp,
                            )
                        }

                        // Metadata (Time & Read Status)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(
                                text = SimpleDateFormat(
                                    "hh:mm aa", Locale.getDefault()
                                ).format(message.timestamp),
                                color = textColor.copy(alpha = 0.7f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )

                            if (isMe) {
                                Icon(
                                    imageVector = when (message.status) {
                                        "sending" -> Icons.Default.AccessTime
                                        "send" -> Icons.Default.DoneAll
                                        else -> Icons.Default.ErrorOutline
                                    },
                                    contentDescription = null,
                                    tint = if (message.isRead) Color(0xFF81D4FA) else textColor.copy(
                                        alpha = 0.7f
                                    ),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                if (!message.reaction.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier.padding(horizontal = 4.dp) // बबल के अलाइनमेंट के हिसाब से थोड़ा अंदर
                    ) {
                        MessageReactionBadge(reaction = message.reaction!!)
                    }
                }
            }
        }


        if (showReactionPicker == message) {
            ReactionPopup(onDismiss = { showReactionPicker = null }, onReact = { emoji ->
                viewModel.sendReaction(message, emoji, user = user)
                showReactionPicker = null
            })
        }
    }
}

@Composable
fun MessageReactionBadge(reaction: String) {
    Surface(
        shape = CircleShape,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier
            .offset(y = (-12).dp)
            .size(24.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = reaction, fontSize = 12.sp)
        }
    }
}

@Composable
fun ReactionPopup(
    onDismiss: () -> Unit, onReact: (String) -> Unit
) {
    val emojis = listOf("❤️", "👍", "😂", "😮", "😢", "🙏", "😘")

    // Popup का उपयोग करके इसे लेयर के ऊपर दिखाएं
    Popup(
        alignment = Alignment.TopCenter,
        onDismissRequest = onDismiss,
        offset = IntOffset(0, -120) // बबल के थोड़ा ऊपर दिखाने के लिए
    ) {
        Surface(
            modifier = Modifier
                .shadow(8.dp, RoundedCornerShape(30.dp))
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(30.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            shape = RoundedCornerShape(30.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                emojis.forEach { emoji ->
                    Text(
                        text = emoji,
                        fontSize = 24.sp,
                        modifier = Modifier
                            .clickable { onReact(emoji) }
                            .padding(8.dp))
                }
            }
        }
    }
}

@Composable
fun ImageGrid(
    imageUris: List<String>,
    modifier: Modifier = Modifier,
    isNetworkImage: Boolean = false,
    onImageClick: (index: Int) -> Unit
) {
    // Dynamic calculation for grid layout
    val (columns, itemSize) = when (imageUris.size) {
        1 -> 1 to 250.dp
        2, 4 -> 2 to 125.dp
        else -> 3 to 85.dp // 3 or more images
    }

    // Calculate total height to fix layout inside LazyColumn
    val rowCount = ceil(imageUris.size.toFloat() / columns).toInt()
    val totalHeight = (itemSize * rowCount) + (rowCount - 1).coerceAtLeast(0) * 4.dp

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier
            .width(if (columns == 1) 250.dp else 254.dp) // Fixed width for consistent bubbles
            .height(totalHeight),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        userScrollEnabled = false
    ) {
        itemsIndexed(imageUris) { index, uri ->
            val commonModifier = Modifier
                .size(itemSize) // Force square/rect size
                .clip(RoundedCornerShape(8.dp))
                .clickable { onImageClick(index) }
                .background(Color.LightGray) // Placeholder color while loading

            if (isNetworkImage) {
                AsyncImage(
                    model = uri,
                    contentDescription = "Image",
                    modifier = commonModifier,
                    contentScale = ContentScale.Crop
                )
            } else {
                SubcomposeAsyncImage(
                    model = uri,
                    contentDescription = "Image",
                    modifier = commonModifier,
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}


@Composable
fun ModernReplyPreviewMessage(message: String, isMe: Boolean) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.1f)) // Darker overlay
            .padding(8.dp)
            .width(IntrinsicSize.Max)
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .background(if (isMe) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = message,
            color = if (isMe) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurface,
            fontSize = 13.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ModernChatInputBar(
    navController: NavController,
    viewModel: MessageViewModel,
    user: User?,
    modifier: Modifier = Modifier,
    repliedMessage: Messages?,
    onReplyDismiss: () -> Unit
) {
    var text by remember { mutableStateOf("") }
    val context = LocalContext.current
    val isTyping = text.isNotBlank()

    val recorder by remember { mutableStateOf(AudioRecorder(context)) }
    var audioFile by remember { mutableStateOf<File?>(null) }

    // Launcher for Gallery
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(10)
    ) { uris ->
        if (uris.isNotEmpty()) {
            uris.forEach { uri ->
                try {
                    val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    context.contentResolver.takePersistableUriPermission(uri, flag)
                } catch (e: Exception) {
                    Log.e("PhotoPicker", "परमिशन नहीं मिल पाई: ${e.message}")
                }
            }
            viewModel.sendImage(uris, user!!)
        }
    }

    val capturedImageUri =
        navController.currentBackStackEntry?.savedStateHandle?.getStateFlow<String?>(
            "capturedImageUri", null
        )?.collectAsState()

    LaunchedEffect(capturedImageUri?.value) {

        capturedImageUri?.value?.let { uriString ->
            val uri = Uri.parse(uriString)
            viewModel.sendImage(listOf(uri), user!!)

            navController.currentBackStackEntry?.savedStateHandle?.remove<String>("capturedImageUri")

        }

    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        // 1. Reply Preview
        AnimatedVisibility(
            visible = repliedMessage != null,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()) {
            repliedMessage?.let { msg ->
                ReplyPreview(message = msg, onDismiss = onReplyDismiss)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom
        ) {
            // 2. Input Container
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Attachment Icon
                    IconButton(onClick = {
                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }) {
                        Icon(
                            Icons.Default.AttachFile,
                            contentDescription = "Attach",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Text Field
                    BasicTextField(
                        value = text,
                        onValueChange = {
                            text = it
                            viewModel.setTyping(it.isNotEmpty())
                        },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences, // हर वाक्य का पहला अक्षर Capital करेगा
                            imeAction = ImeAction.Default
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 12.dp),
                        textStyle = LocalTextStyle.current.copy(
                            color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        decorationBox = { innerTextField ->
                            if (text.isEmpty()) {
                                Text("Message", color = Color.Gray, fontSize = 16.sp)
                            }
                            innerTextField()
                        })

                    // Camera Icon (Visible only when NOT typing)
                    if (!isTyping) {
                        IconButton(onClick = { navController.navigate("camera") }) {
                            Icon(
                                Icons.Default.PhotoCamera,
                                contentDescription = "Camera",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 3. Action Button (Send or Record)
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary, shape = CircleShape
                    ), contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = isTyping, transitionSpec = {
                        scaleIn() togetherWith scaleOut()
                    }, label = ""
                ) { typing ->
                    Icon(
                        imageVector = if (typing) Icons.AutoMirrored.Filled.Send else Icons.Default.Mic,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp).pointerInput(Unit) {
                            detectTapGestures(
//                                onPress = {
//                                    // 1. बटन दबाते ही रिकॉर्डिंग शुरू
//                                    if(!isTyping){
//                                        audioFile = File(context.cacheDir, "rec_${System.currentTimeMillis()}.mp3")
//                                        recorder.start(audioFile!!)
//
//                                        tryAwaitRelease() // जब तक उंगली न हटाए, इंतज़ार करें
//
//                                        // 2. उंगली हटाते ही रिकॉर्डिंग स्टॉप
//                                        recorder.stop()
//
//                                        // 3. ViewModel के जरिए ऑडियो भेजें (जैसे इमेज भेजते हैं)
//                                        viewModel.sendAudio(Uri.fromFile(audioFile!!), user!!)
//                                    }
//                                },
                                onTap = {
                                    if (isTyping) {
                                        viewModel.sendMessage(
                                            message = text, user = user!!, replyMessage = repliedMessage
                                        )
                                        text = ""
                                        onReplyDismiss()
                                        viewModel.setTyping(false)
                                    } else {
                                        // TODO: Implement Audio Recording Logic
                                        Toast.makeText(context, "Hold to record", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationTopAppBar(
    navController: NavController, user: User
) {
    // Glassmorphism-lite using semi-transparent surface
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f), shadowElevation = 2.dp
    ) {
        TopAppBar(
            title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { /* Open Profile */ }) {
                Box {
                    AsyncImage(
                        model = user.imageUrl,
                        contentDescription = null,
                        placeholder = painterResource(R.drawable.boy),
                        error = painterResource(R.drawable.boy),
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape) // Standard Circle for modern look
                            .background(Color.LightGray),
                        contentScale = ContentScale.Crop
                    )
                    if (user.online == true) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .align(Alignment.BottomEnd)
                                .clip(CircleShape)
                                .background(Color(0xFF4CAF50))
                                .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
                        )
                    }
                }

                Spacer(Modifier.width(10.dp))

                Column {
                    Text(
                        text = user.name ?: "User",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Modern status text
                    val statusText = when {
                        user.online == true -> "Active now"
                        else -> formatLastSeen(user.lastSeen)
                    }

                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (user.online == true) Color(0xFF4CAF50) else Color.Gray
                    )
                }
            }
        }, navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
            }
        }, actions = {
            IconButton(onClick = { /* Call */ }) {
                Icon(Icons.Default.Phone, null, tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = {
                navController.currentBackStackEntry?.savedStateHandle?.apply {
                    set("user_name", user.name)
                    set("user_uid", user.id)
                    set("user_token", user.token)
                }
                navController.navigate("video-call")
            }) {
                Icon(Icons.Default.Videocam, null, tint = MaterialTheme.colorScheme.primary)
            }
        }, colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent // Let Surface handle color
        )
        )
    }
}

@Composable
fun ModernDateHeader(timestamp: Long) {
    val dateStr = SimpleDateFormat("MMMM dd", Locale.getDefault()).format(timestamp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = dateStr,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun TypingBubble() {
    // Match the "Receiver" (Other user) bubble color
    val bubbleColor = if (isSystemInDarkTheme()) Color(0xFF333333) else Color(0xFFF2F4F7)

    // Match the "Receiver" shape (TopLeft sharp, others rounded)
    val bubbleShape =
        RoundedCornerShape(topStart = 4.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 0.dp), // Align with message list padding
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .padding(start = 12.dp) // Left margin
                .clip(bubbleShape)
                .background(bubbleColor)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            TypingIndicator(
                dotSize = 7.dp,
                bounceHeight = 5.dp,
                dotColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun TypingIndicator(
    modifier: Modifier = Modifier,
    dotSize: Dp = 8.dp,
    spaceBetweenDots: Dp = 4.dp,
    bounceHeight: Dp = 6.dp,
    dotColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "typing_animation")

    // Animate each of the 3 dots with a staggered delay
    val animations = List(3) { index ->
        infiniteTransition.animateFloat(
            initialValue = 0f, targetValue = 1f, animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 500, easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse,
                initialStartOffset = StartOffset(index * 150) // Stagger start time
            ), label = "dot_$index"
        )
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spaceBetweenDots),
        verticalAlignment = Alignment.CenterVertically
    ) {
        animations.forEach { anim ->
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .offset(y = -(bounceHeight * anim.value)) // Move up/down
                    .background(color = dotColor, shape = CircleShape)
            )
        }
    }
}

@Composable
fun ReplyPreview(
    message: Messages, onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp) // Spacing inside the container
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)) // Subtle gray background
            .padding(12.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        // Vertical Accent Line
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(38.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.primary)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Text Content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Replying to",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = message.messageContent ?: "Photo",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Close Button
        IconButton(
            onClick = onDismiss, modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Cancel reply",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}


private fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = timestamp1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = timestamp2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(
        Calendar.DAY_OF_YEAR
    )
}
