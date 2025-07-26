package com.tinhtx.localplayerapplication.presentation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.tinhtx.localplayerapplication.core.constants.AppConstants
import com.tinhtx.localplayerapplication.core.utils.NotificationChannelHelper
import com.tinhtx.localplayerapplication.core.utils.PermissionUtils
import com.tinhtx.localplayerapplication.presentation.navigation.LocalPlayerNavigation
import com.tinhtx.localplayerapplication.presentation.navigation.NavigationEvent
import com.tinhtx.localplayerapplication.presentation.theme.LocalPlayerTheme
import com.tinhtx.localplayerapplication.presentation.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val mainViewModel: MainViewModel by viewModels()
    
    private var splashScreenKeepOnScreen = true
    
    private val permissionRequestLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        mainViewModel.onPermissionsResult(allGranted, permissions)
        
        if (allGranted) {
            lifecycleScope.launch {
                mainViewModel.startInitialMediaScan()
            }
        } else {
            val deniedPermissions = permissions.filterValues { !it }.keys.toList()
            mainViewModel.onPermissionsDenied(deniedPermissions)
        }
    }
    
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        
        super.onCreate(savedInstanceState)
        
        setupSplashScreen(splashScreen)
        setupSystemUI()
        createNotificationChannels()
        checkAndRequestPermissions()
        
        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
            
            LocalPlayerTheme(
                darkTheme = when (uiState.themeMode) {
                    AppConstants.ThemeMode.LIGHT -> false
                    AppConstants.ThemeMode.DARK -> true
                    AppConstants.ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
                },
                dynamicColor = uiState.useDynamicColors
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when {
                        uiState.isLoading -> {
                            LoadingScreen()
                        }
                        !uiState.hasRequiredPermissions -> {
                            PermissionRequestScreen(
                                onRequestPermissions = ::checkAndRequestPermissions,
                                onOpenSettings = ::openAppSettings
                            )
                        }
                        uiState.hasError -> {
                            ErrorScreen(
                                error = uiState.errorMessage ?: "Unknown error occurred",
                                onRetry = {
                                    mainViewModel.clearError()
                                    checkAndRequestPermissions()
                                }
                            )
                        }
                        else -> {
                            LocalPlayerNavigation(
                                windowSizeClass = windowSizeClass,
                                startDestination = if (uiState.isFirstLaunch) {
                                    Screen.Home.route
                                } else {
                                    uiState.lastRoute ?: Screen.Home.route
                                },
                                onDeepLinkReceived = { intent ->
                                    handleDeepLink(intent)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleDeepLink(it) }
    }
    
    override fun onResume() {
        super.onResume()
        mainViewModel.onAppResumed()
    }
    
    override fun onPause() {
        super.onPause()
        mainViewModel.onAppPaused()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        mainViewModel.onAppDestroyed()
    }
    
    private fun setupSplashScreen(splashScreen: SplashScreen) {
        splashScreen.setKeepOnScreenCondition { splashScreenKeepOnScreen }
        
        lifecycleScope.launch {
            delay(1500) // Minimum splash screen duration
            val isReady = mainViewModel.waitForInitialization()
            if (isReady) {
                splashScreenKeepOnScreen = false
            }
        }
    }
    
    private fun setupSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
    }
    
    private fun createNotificationChannels() {
        NotificationChannelHelper.createNotificationChannels(this)
    }
    
    private fun checkAndRequestPermissions() {
        val requiredPermissions = getRequiredPermissions()
        val missingPermissions = requiredPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
        }
        
        if (missingPermissions.isNotEmpty()) {
            permissionRequestLauncher.launch(missingPermissions.toTypedArray())
        } else {
            mainViewModel.onPermissionsGranted()
            lifecycleScope.launch {
                mainViewModel.startInitialMediaScan()
            }
        }
    }
    
    private fun getRequiredPermissions(): List<String> {
        val permissions = mutableListOf<String>()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.addAll(
                listOf(
                    Manifest.permission.READ_MEDIA_AUDIO,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            )
        } else {
            permissions.addAll(
                listOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }
        
        permissions.add(Manifest.permission.WAKE_LOCK)
        permissions.add(Manifest.permission.FOREGROUND_SERVICE)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            permissions.add(Manifest.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK)
        }
        
        return permissions
    }
    
    private fun openAppSettings() {
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }
    
    private fun handleDeepLink(intent: Intent) {
        lifecycleScope.launch {
            val uri = intent.data
            val source = intent.getStringExtra("source") ?: "external"
            
            mainViewModel.handleDeepLink(uri, source)
            
            android.util.Log.d("MainActivity", "Deep link handled: $uri from $source")
        }
    }
    
    @Composable
    private fun LoadingScreen() {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            androidx.compose.foundation.layout.Column(
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                androidx.compose.material3.CircularProgressIndicator(
                    modifier = androidx.compose.ui.Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))
                androidx.compose.material3.Text(
                    text = "Loading your music library...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
    
    @Composable
    private fun PermissionRequestScreen(
        onRequestPermissions: () -> Unit,
        onOpenSettings: () -> Unit
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            androidx.compose.material3.Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.MusicNote,
                contentDescription = null,
                modifier = androidx.compose.ui.Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(24.dp))
            
            androidx.compose.material3.Text(
                text = "Permission Required",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))
            
            androidx.compose.material3.Text(
                text = "LocalPlayer needs access to your music files to provide the best experience. Please grant the required permissions.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(32.dp))
            
            androidx.compose.material3.Button(
                onClick = onRequestPermissions,
                modifier = androidx.compose.ui.Modifier.fillMaxWidth()
            ) {
                androidx.compose.material3.Text("Grant Permissions")
            }
            
            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))
            
            androidx.compose.material3.TextButton(
                onClick = onOpenSettings
            ) {
                androidx.compose.material3.Text("Open Settings")
            }
        }
    }
    
    @Composable
    private fun ErrorScreen(
        error: String,
        onRetry: () -> Unit
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            androidx.compose.material3.Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.Error,
                contentDescription = null,
                modifier = androidx.compose.ui.Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            
            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(24.dp))
            
            androidx.compose.material3.Text(
                text = "Something went wrong",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))
            
            androidx.compose.material3.Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(32.dp))
            
            androidx.compose.material3.Button(
                onClick = onRetry,
                modifier = androidx.compose.ui.Modifier.fillMaxWidth()
            ) {
                androidx.compose.material3.Text("Try Again")
            }
        }
    }
}
