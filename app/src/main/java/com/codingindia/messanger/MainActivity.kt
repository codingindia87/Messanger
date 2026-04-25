package com.codingindia.messanger

import android.os.Bundle
import android.os.StrictMode
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.IntOffset
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.codingindia.messanger.features.addchat.AddChats
import com.codingindia.messanger.features.auth.signin.LoginScreen
import com.codingindia.messanger.features.fullscreenimage.FullScreenImageSlider
import com.codingindia.messanger.features.fullscreenimage.SharedViewModel
import com.codingindia.messanger.features.home.CameraScreen
import com.codingindia.messanger.features.home.HomeScreen
import com.codingindia.messanger.features.home.PresenceViewModel
import com.codingindia.messanger.features.home.updates.StatusViewerScreen
import com.codingindia.messanger.features.message.MessageScreen
import com.codingindia.messanger.features.settings.SettingScreen
import com.codingindia.messanger.features.updates.UpdateScreen
import com.codingindia.messanger.features.videocall.VideoCallScreen
import com.codingindia.messanger.features.zoomableImage.FullScreenImageScreen
import com.codingindia.messanger.ui.theme.MessangerTheme
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val user = FirebaseAuth.getInstance().currentUser
        val start = if (user != null) "home" else "login"
        val animationSpec = tween<IntOffset>(durationMillis = 300)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            MessangerTheme {
                val navController = rememberNavController()
                val viewModel: PresenceViewModel = hiltViewModel()

                val lifecycleOwner = LocalLifecycleOwner.current
                val observer = remember {
                    LifecycleEventObserver { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_START -> {
                                viewModel.onAppStart()
                            }

                            Lifecycle.Event.ON_STOP -> {
                                viewModel.onAppStop()
                            }

                            else -> {
                                // Do nothing
                            }
                        }
                    }
                }

                DisposableEffect(lifecycleOwner) {
                    lifecycleOwner.lifecycle.addObserver(observer)

                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }


                NavHost(navController = navController, startDestination = start) {
                    composable("login") {
                        LoginScreen(navController)
                    }
                    composable("home") {
                        HomeScreen(navController)
                    }
                    composable(route = "add-chat") {
                        AddChats(navController)
                    }
                    composable(
                        route = "message/{uid}",
                        deepLinks = listOf(navDeepLink {
                            uriPattern = "app://chat/{uid}"
                        }),
                        arguments = listOf(
                            navArgument("uid") { type = NavType.StringType },
                        ),
                        enterTransition = {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Left, animationSpec
                            )
                        },
                        popExitTransition = {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Right, animationSpec
                            )
                        },
                    ) { backStackEntry ->
                        val userId = backStackEntry.arguments?.getString("uid")
                        MessageScreen(
                            navController = navController, uid = userId!!
                        )
                    }
                    composable(
                        route = "fullScreenImage/{imageUrl}",
                        arguments = listOf(navArgument("imageUrl") { type = NavType.StringType }),
                        // Animation when opening the screen (Zoom In + Fade In)
                        enterTransition = {
                            scaleIn(
                                initialScale = 0.8f, animationSpec = tween(300)
                            ) + fadeIn(animationSpec = tween(300))
                        },
                        // Animation when closing the screen/going back (Zoom Out + Fade Out)
                        popExitTransition = {
                            scaleOut(
                                targetScale = 0.8f, animationSpec = tween(300)
                            ) + fadeOut(animationSpec = tween(300))
                        }) { backStackEntry ->
                        val encodedUrl = backStackEntry.arguments?.getString("imageUrl")
                        val decodedUrl = if (encodedUrl != null) {
                            try {
                                String(Base64.decode(encodedUrl, Base64.URL_SAFE))
                            } catch (e: Exception) {
                                null
                            }
                        } else {
                            null
                        }

                        // Pass the decoded URL to your screen
                        if (decodedUrl != null) {
                            FullScreenImageScreen(url = decodedUrl, navController = navController)
                        }
                    }
                    composable("camera") {
                        CameraScreen(onImageCaptured = { uri ->
                            navController.previousBackStackEntry?.savedStateHandle?.set(
                                "capturedImageUri", uri.toString()
                            )

                            navController.popBackStack() // return to MessageScreen
                        }, onBack = { navController.popBackStack() })
                    }
                    composable(
                        route = "imageSlider/{initialIndex}",
                        arguments = listOf(navArgument("initialIndex") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val initialIndex = backStackEntry.arguments?.getInt("initialIndex") ?: 0
                        val parentEntry = remember(backStackEntry) {
                            navController.getBackStackEntry("message/{uid}")
                        }
                        val sharedViewModel: SharedViewModel = hiltViewModel(parentEntry)
                        val imageUris by sharedViewModel.imageUris.collectAsState()
                        FullScreenImageSlider(
                            imageUris = imageUris, initialIndex = initialIndex, onClose = {
                                navController.popBackStack()
                            })
                    }

                    composable(route = "settings", enterTransition = {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left, animationSpec
                        )
                    }, popExitTransition = {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right, animationSpec
                        )
                    }) {
                        val savedStateHandle =
                            navController.previousBackStackEntry?.savedStateHandle
                        val userName = savedStateHandle?.get<String>("userName") ?: "Unknow user"
                        val imageUrl = savedStateHandle?.get<String>("imageUrl") ?: ""
                        val email = savedStateHandle?.get<String>("email") ?: ""
                        SettingScreen(
                            userName = userName,
                            email = email,
                            userImage = imageUrl,
                            navController = navController
                        )
                    }

                    composable("view_status") {
                        val savedStateHandle =
                            navController.previousBackStackEntry?.savedStateHandle
                        val urls = savedStateHandle?.get<List<String>>("status_urls") ?: emptyList()
                        val userName = savedStateHandle?.get<String>("status_user_name") ?: "User"
                        val userImage = savedStateHandle?.get<String>("status_user_image") ?: ""
                        val timeAgo = savedStateHandle?.get<String>("ago_time") ?: ""
                        val id = savedStateHandle?.get<String>("id") ?: ""

                        StatusViewerScreen(
                            urls = urls,
                            userName = userName,
                            userImage = userImage,
                            timeAgo = timeAgo,
                            id = id,
                            onBack = {
                                // 1. Clear Data to prevent loops/stale data
                                savedStateHandle?.remove<List<String>>("status_urls")

                                // 2. Pop Back Stack safely
                                if (navController.currentBackStackEntry?.lifecycle?.currentState?.isAtLeast(
                                        Lifecycle.State.RESUMED
                                    ) == true
                                ) {
                                    navController.popBackStack()
                                }
                            })
                    }

                    composable("update", enterTransition = {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left, animationSpec
                        )
                    }, popExitTransition = {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right, animationSpec
                        )
                    }) {
                        UpdateScreen(onDismiss = {
                            navController.popBackStack()
                        })
                    }

                    composable("video-call", enterTransition = {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left, animationSpec
                        )
                    }, popExitTransition = {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right, animationSpec
                        )
                    }) {
                        val savedStateHandle =
                            navController.previousBackStackEntry?.savedStateHandle
                        val userName = savedStateHandle?.get<String>("user_name") ?: "User"
                        val userUID = savedStateHandle?.get<String>("user_uid") ?: ""
                        val userToken = savedStateHandle?.get<String>("user_token") ?: ""
                        VideoCallScreen(navController, userName, userUID, userToken)
                    }
                }
            }
        }

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

    }

}
