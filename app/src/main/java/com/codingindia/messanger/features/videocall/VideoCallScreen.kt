package com.codingindia.messanger.features.videocall

import android.Manifest
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun VideoCallScreen(
    navController: NavController,
    userName: String,
    userUID: String,
    userToken: String,
    viewModel: VideoCallingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 1. Permission State
    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    )

    // Camera Selector State (Front by default)
    var cameraSelector by remember { mutableStateOf(CameraSelector.DEFAULT_FRONT_CAMERA) }

    // Launch permission request
    LaunchedEffect(Unit) {
        permissionState.launchMultiplePermissionRequest()
        viewModel.startCall(userName, userToken, userUID)
    }


    Scaffold(topBar = {
        CenterAlignedTopAppBar(
            title = { Text(userName, color = Color.White) },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.Black.copy(alpha = 0.4f)
            )
        )
    }, bottomBar = {
        BottomAppBar(
            containerColor = Color.Transparent, modifier = Modifier.padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CallActionBtn(Icons.Default.Mic, Color.Gray) { /* Mute logic */ }
                CallActionBtn(Icons.Default.CallEnd, Color.Red) {
                    viewModel.endCall(userToken, userUID)
                    navController.popBackStack()
                }

                // 2. Camera Flip Button
                CallActionBtn(Icons.Default.FlipCameraAndroid, Color.Gray) {
                    cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
                        CameraSelector.DEFAULT_BACK_CAMERA
                    } else {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    }
                }
                CallActionBtn(Icons.Default.VideocamOff, Color.Gray) { }
            }
        }
    }) { innerPadding ->
        if (permissionState.allPermissionsGranted) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color.Black)
            ) {

                // --- FULL SCREEN (Remote User Placeholder) ---
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Remote Video Feed (Waiting...)", color = Color.Gray)
                }

                // --- PIP BOX (Local Camera Preview) ---
                Surface(
                    modifier = Modifier
                        .size(width = 130.dp, height = 180.dp)
                        .padding(16.dp)
                        .align(Alignment.TopEnd),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black,
                    tonalElevation = 8.dp
                ) {
                    // CameraX Preview View
                    AndroidView(factory = { ctx ->
                        PreviewView(ctx).apply {
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                        }
                    }, modifier = Modifier.fillMaxSize(), update = { previewView ->
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.surfaceProvider = previewView.surfaceProvider
                            }
                            try {
                                cameraProvider.unbindAll() // पुराना कैमरा बंद करें
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner, cameraSelector, // नया कैमरा बाइंड करें
                                    preview
                                )
                            } catch (e: Exception) {
                                Log.e("CameraX", "Flip Failed", e)
                            }
                        }, ContextCompat.getMainExecutor(context))
                    })
                }
            }
        } else {
            // Permission Denied View
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Please grant Camera and Audio permissions to continue.", color = Color.Red)
            }
        }
    }
}


@Composable
fun CallActionBtn(icon: ImageVector, bgColor: Color, onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = bgColor,
        contentColor = Color.White,
        shape = CircleShape,
        modifier = Modifier.size(56.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null)
    }
}