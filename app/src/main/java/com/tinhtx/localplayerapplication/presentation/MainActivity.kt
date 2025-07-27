package com.tinhtx.localplayerapplication.presentation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import com.tinhtx.localplayerapplication.presentation.components.common.*
import com.tinhtx.localplayerapplication.presentation.components.ui.*
import com.tinhtx.localplayerapplication.presentation.navigation.*
import com.tinhtx.localplayerapplication.presentation.theme.LocalPlayerTheme
import com.tinhtx.localplayerapplication.presentation.viewmodel.MainViewModel
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val mainViewModel: MainViewModel by viewModels()
    
    @Inject
    lateinit var deepLinkHandler: DeepLinkHandler
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        mainViewModel.onPermissionsResult(allGranted)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen
        val splashScreen = installSplashScreen()
        
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display
        enableEdgeToEdge()
        
        // Configure splash screen
        splashScreen.setKeepOnScreenCondition {
            mainViewModel.isLoading.value
        }
        
        // Check and request permissions
        checkPermissions()
        
        // Handle deep links
        handleDeepLink(intent)
        
        setContent {
            LocalPlayerApp(
                mainViewModel = mainViewModel,
                deepLinkHandler = deepLinkHandler
            )
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }
    
    private fun checkPermissions() {
        val requiredPermissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_MEDIA_AUDIO
        )
        
        val missingPermissions = requiredPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
        }
        
        if (missingPermissions.isNotEmpty()) {
            permissionLauncher.launch(missingPermissions.toTypedArray())
        } else {
            mainViewModel.onPermissionsGranted()
        }
    }
    
    private fun handleDeepLink(intent: Intent) {
        intent.data?.let { uri ->
            mainViewModel.handleDeepLink(uri)
        }
    }
}

@Composable
fun LocalPlayerApp(
    mainViewModel: MainViewModel,
    deepLinkHandler: DeepLinkHandler
) {
    val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
    
    LocalPlayerTheme(
        darkTheme = uiState.isDarkTheme,
        dynamicColor = uiState.useDynamicColors
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when (uiState.appState) {
                AppState.Loading -> {
                    LoadingScreen(
                        title = "LocalPlayer",
                        subtitle = "Loading your music library..."
                    )
                }
                
                AppState.NoPermissions -> {
                    PermissionScreen(
                        onRequestPermissions = mainViewModel::requestPermissions
                    )
                }
                
                AppState.EmptyLibrary -> {
                    EmptyLibraryScreen(
                        onScanLibrary = mainViewModel::scanMusicLibrary,
                        onRequestPermissions = mainViewModel::requestPermissions
                    )
                }
                
                AppState.Ready -> {
                    MainAppContent(
                        mainViewModel = mainViewModel,
                        deepLinkHandler = deepLinkHandler,
                        uiState = uiState
                    )
                }
                
                AppState.Error -> {
                    ErrorScreen(
                        title = "Something went wrong",
                        message = uiState.errorMessage ?: "An unexpected error occurred",
                        onRetry = mainViewModel::retry,
                        onNavigateBack = { /* Handle navigation */ }
                    )
                }
            }
        }
    }
}

@Composable
private fun MainAppContent(
    mainViewModel: MainViewModel,
    deepLinkHandler: DeepLinkHandler,
    uiState: MainUiState
) {
    val navController = rememberNavController()
    val navigationState = rememberLocalPlayerNavigationState(
        navController = navController,
        deepLinkHandler = deepLinkHandler
    )
    
    // Handle deep links from ViewModel
    LaunchedEffect(uiState.pendingDeepLink) {
        uiState.pendingDeepLink?.let { uri ->
            deepLinkHandler.handleDeepLink(uri, navController)
            mainViewModel.clearPendingDeepLink()
        }
    }
    
    // Navigation analytics
    NavigationObserver(
        navController = navController,
        onNavigationEvent = mainViewModel::trackNavigation
    )
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Main navigation content
        ResponsiveNavigationLayout(
            items = uiState.navigationItems,
            selectedItem = navigationState.currentRoute ?: NavDestinations.HOME,
            onItemClick = { route ->
                navigationState.navigateTo(route)
            },
            hasNotification = uiState.navigationBadges,
            topBar = {
                // Top app bar will be handled by individual screens
            },
            floatingActionButton = {
                // FAB for quick actions if needed
            }
        ) { paddingValues ->
            LocalPlayerNavigation(
                navController = navController,
                startDestination = uiState.startDestination,
                deepLinkHandler = deepLinkHandler,
                modifier = Modifier.padding(paddingValues)
            )
        }
        
        // Mini player overlay
        AnimatedVisibility(
            visible = uiState.showMiniPlayer && uiState.currentSong != null,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            if (uiState.currentSong != null) {
                com.tinhtx.localplayerapplication.presentation.components.music.MiniPlayer(
                    currentSong = uiState.currentSong,
                    isPlaying = uiState.isPlaying,
                    progress = uiState.playbackProgress,
                    onPlayPause = mainViewModel::togglePlayback,
                    onSkipNext = mainViewModel::skipNext,
                    onSkipPrevious = mainViewModel::skipPrevious,
                    onClick = {
                        navigationState.navigateToPlayer()
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
        
        // Player bottom sheet
        PlayerBottomSheet(
            currentSong = uiState.currentSong,
            isPlaying = uiState.isPlaying,
            progress = uiState.playbackProgress,
            currentPosition = uiState.currentPosition,
            duration = uiState.duration,
            isShuffleEnabled = uiState.isShuffleEnabled,
            repeatMode = uiState.repeatMode,
            isVisible = uiState.showPlayerSheet,
            onDismiss = mainViewModel::hidePlayerSheet,
            onPlayPause = mainViewModel::togglePlayback,
            onSkipNext = mainViewModel::skipNext,
            onSkipPrevious = mainViewModel::skipPrevious,
            onSeek = mainViewModel::seekTo,
            onToggleShuffle = mainViewModel::toggleShuffle,
            onToggleRepeat = mainViewModel::toggleRepeat,
            onToggleFavorite = mainViewModel::toggleFavorite,
            isFavorite = uiState.isFavorite
        )
    }
}

@Composable
private fun PermissionScreen(
    onRequestPermissions: () -> Unit
) {
    EmptyState(
        icon = androidx.compose.material.icons.Icons.Default.LibraryMusic,
        title = "Permission Required",
        description = "LocalPlayer needs access to your device storage to play your music files.",
        actionText = "Grant Permission",
        onActionClick = onRequestPermissions
    )
}

@Composable
private fun EmptyLibraryScreen(
    onScanLibrary: () -> Unit,
    onRequestPermissions: () -> Unit
) {
    EmptyState(
        icon = androidx.compose.material.icons.Icons.Default.MusicNote,
        title = "No Music Found",
        description = "We couldn't find any music files on your device. Make sure you have music files stored locally.",
        actionText = "Scan for Music",
        onActionClick = onScanLibrary,
        secondaryActionText = "Check Permissions",
        onSecondaryActionClick = onRequestPermissions
    )
}
