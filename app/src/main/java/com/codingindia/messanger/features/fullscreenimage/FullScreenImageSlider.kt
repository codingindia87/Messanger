package com.codingindia.messanger.features.fullscreenimage

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlin.math.max

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FullScreenImageSlider(
    imageUris: List<String>,
    initialIndex: Int,
    onClose: () -> Unit
) {
    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { imageUris.size }
    )

    var showControls by remember { mutableStateOf(true) }
    var isZoomed by remember { mutableStateOf(false) }

    // Drag to dismiss state
    var offsetY by remember { mutableFloatStateOf(0f) }
    val dismissThreshold = 150f

    Scaffold(
        containerColor = Color.Black,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.Black)
                // 1. Drag to Dismiss (Vertical only)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { showControls = !showControls })
                }
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationY = offsetY
                        alpha = 1f - (offsetY / 600f).coerceIn(0f, 1f)
                    },
                userScrollEnabled = !isZoomed // Only scroll if image is NOT zoomed
            ) { page ->
                ZoomableImage(
                    imageUrl = imageUris[page],
                    isZoomed = isZoomed,
                    onZoomChanged = { zoomed -> isZoomed = zoomed },
                    onVerticalDrag = { dragAmount ->
                        // Only allow vertical drag if NOT zoomed
                        if (!isZoomed) {
                            offsetY = max(0f, offsetY + dragAmount)
                        }
                    },
                    onDragEnd = {
                        if (offsetY > dismissThreshold) onClose() else offsetY = 0f
                    }
                )
            }

            // --- UI Overlays (Same as before) ---
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(Color.Black.copy(0.7f), Color.Transparent)))
                        .statusBarsPadding()
                        .padding(8.dp)
                ) {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                }
            }

            AnimatedVisibility(
                visible = showControls && imageUris.size > 1,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.7f))))
                        .navigationBarsPadding()
                        .padding(bottom = 30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(color = Color.White.copy(0.2f), shape = CircleShape) {
                        Text(
                            "${pagerState.currentPage + 1} / ${imageUris.size}",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ZoomableImage(
    imageUrl: String,
    isZoomed: Boolean,
    onZoomChanged: (Boolean) -> Unit,
    onVerticalDrag: (Float) -> Unit,
    onDragEnd: () -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val configuration = LocalConfiguration.current
    val screenWidth = with(LocalDensity.current) { configuration.screenWidthDp.dp.toPx() }
    val screenHeight = with(LocalDensity.current) { configuration.screenHeightDp.dp.toPx() }

    // Transformable state handles pinch-to-zoom efficiently without blocking paging
    val state = rememberTransformableState { zoomChange, panChange, _ ->
        val newScale = (scale * zoomChange).coerceIn(1f, 5f)
        scale = newScale
        onZoomChanged(scale > 1f)

        if (scale > 1f) {
            // While zoomed, apply pan to the image
            val maxX = (screenWidth * scale - screenWidth) / 2
            val maxY = (screenHeight * scale - screenHeight) / 2

            offset = Offset(
                x = (offset.x + panChange.x * scale).coerceIn(-maxX, maxX),
                y = (offset.y + panChange.y * scale).coerceIn(-maxY, maxY)
            )
        } else {
            // If not zoomed, pass vertical drags up to parent for "Dismiss" logic
            onVerticalDrag(panChange.y)
            offset = Offset.Zero
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            // 1. Handle Double Tap
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { tapOffset ->
                        val targetScale = if (scale > 1f) 1f else 2.5f
                        scale = targetScale
                        onZoomChanged(scale > 1f)
                        if (scale == 1f) offset = Offset.Zero
                    }
                )
            }
            // 2. Handle Zoom/Pan AND Vertical Dismiss logic
            .transformable(state = state)
            // 3. Detect when finger is lifted to trigger dismiss animation
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.changes.any { !it.pressed }) {
                            onDragEnd()
                        }
                    }
                }
            }
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                },
            contentScale = ContentScale.Fit
        )
    }
}
