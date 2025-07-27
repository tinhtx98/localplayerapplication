package com.tinhtx.localplayerapplication.presentation.screens.settings

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.tinhtx.localplayerapplication.presentation.screens.settings.components.*

/**
 * Settings Screen - Main settings interface
 * Maps với SettingsViewModel và SettingsUiState
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAbout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // Handle error auto-dismiss
    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            kotlinx.coroutines.delay(5000)
            viewModel.clearError()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        SwipeRefresh(
            state = rememberSwipeRefreshState(uiState.isRefreshing),
            onRefresh = viewModel::refreshSettings,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top app bar
                SettingsTopBar(
                    onNavigateBack = onNavigateBack,
                    onRefresh = viewModel::refreshSettings
                )

                when {
                    uiState.isLoading -> {
                        SettingsLoadingState(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f)
                        )
                    }

                    uiState.hasError -> {
                        SettingsErrorState(
                            error = uiState.currentError ?: "Unknown error",
                            onRetry = viewModel::refreshSettings,
                            onDismiss = viewModel::clearError,
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f)
                        )
                    }

                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Profile Section
                            item {
                                ProfileSection(
                                    userName = uiState.userName,
                                    userEmail = uiState.userEmail,
                                    userAvatar = uiState.userAvatar,
                                    isLoggedIn = uiState.isLoggedIn,
                                    loginMethod = uiState.loginMethod,
                                    onEditProfile = viewModel::toggleProfileDialog,
                                    onLogin = { /* TODO: Navigate to login */ },
                                    onLogout = { /* TODO: Show logout dialog */ }
                                )
                            }

                            // Appearance Settings
                            item {
                                AppearanceSettingsSection(
                                    theme = uiState.theme,
                                    isDynamicColor = uiState.isDynamicColor,
                                    accentColor = uiState.accentColor,
                                    fontSize = uiState.fontSize,
                                    gridSize = uiState.gridSize,
                                    onThemeClick = viewModel::toggleThemeSelectionDialog,
                                    onToggleDynamicColor = viewModel::toggleDynamicColor,
                                    onAccentColorChange = viewModel::updateAccentColor,
                                    onFontSizeChange = viewModel::updateFontSize,
                                    onGridSizeChange = viewModel::updateGridSize
                                )
                            }

                            // Playback Settings
                            item {
                                PlaybackSettingsSection(
                                    audioQuality = uiState.audioQuality,
                                    crossfadeDuration = uiState.crossfadeDuration,
                                    replayGainMode = uiState.replayGainMode,
                                    skipSilence = uiState.skipSilence,
                                    resumeOnHeadphoneConnect = uiState.resumeOnHeadphoneConnect,
                                    pauseOnHeadphoneDisconnect = uiState.pauseOnHeadphoneDisconnect,
                                    ducking = uiState.ducking,
                                    onAudioQualityChange = viewModel::updateAudioQuality,
                                    onCrossfadeDurationChange = viewModel::updateCrossfadeDuration,
                                    onReplayGainModeChange = viewModel::updateReplayGainMode,
                                    onToggleSkipSilence = viewModel::toggleSkipSilence,
                                    onToggleResumeOnHeadphoneConnect = viewModel::toggleResumeOnHeadphoneConnect,
                                    onTogglePauseOnHeadphoneDisconnect = { /* TODO */ },
                                    onToggleDucking = { /* TODO */ },
                                    onEqualizerClick = { /* TODO: Show equalizer */ }
                                )
                            }

                            // Library Settings
                            item {
                                LibrarySettingsSection(
                                    libraryStats = uiState.libraryStats,
                                    lastScanTime = uiState.formattedLastScanTime,
                                    isScanning = uiState.isScanning,
                                    scanProgress = uiState.scanProgress,
                                    scanOnStartup = uiState.scanOnStartup,
                                    includeSubfolders = uiState.includeSubfolders,
                                    onScanLibrary = viewModel::scanLibrary,
                                    onCancelScan = viewModel::cancelLibraryScan,
                                    onToggleScanOnStartup = viewModel::toggleScanOnStartup,
                                    onToggleIncludeSubfolders = { /* TODO */ },
                                    onManageFolders = { /* TODO: Navigate to folder management */ }
                                )
                            }

                            // Notification Settings
                            item {
                                NotificationSettingsSection(
                                    showNotifications = uiState.showNotifications,
                                    showLockScreenControls = uiState.showLockScreenControls,
                                    showAlbumArt = uiState.showAlbumArt,
                                    compactNotification = uiState.compactNotification,
                                    notificationActions = uiState.notificationActions,
                                    onToggleNotifications = { /* TODO */ },
                                    onToggleLockScreenControls = { /* TODO */ },
                                    onToggleAlbumArt = { /* TODO */ },
                                    onToggleCompactNotification = { /* TODO */ },
                                    onCustomizeActions = { /* TODO */ }
                                )
                            }

                            // Storage Settings
                            item {
                                StorageSettingsSection(
                                    cacheSize = uiState.formattedCacheSize,
                                    thumbnailCacheSize = uiState.formattedThumbnailCacheSize,
                                    cacheUsagePercentage = uiState.cacheUsagePercentage,
                                    isCacheNearLimit = uiState.isCacheNearLimit,
                                    maxCacheSize = uiState.formattedMaxCacheSize,
                                    autoClearCache = uiState.autoClearCache,
                                    isClearingCache = uiState.isClearingCache,
                                    onClearCache = viewModel::clearCache,
                                    onManageCache = viewModel::toggleStorageCleanupDialog,
                                    onToggleAutoClearCache = { /* TODO */ }
                                )
                            }

                            // Sleep Timer Settings
                            item {
                                SleepTimerSettings(
                                    isEnabled = uiState.sleepTimerEnabled,
                                    remainingTime = uiState.sleepTimerFormattedRemaining,
                                    duration = uiState.sleepTimerDuration,
                                    action = uiState.sleepTimerAction,
                                    fadeOutDuration = uiState.fadeOutDuration,
                                    onToggleTimer = viewModel::toggleSleepTimerDialog,
                                    onStopTimer = viewModel::stopSleepTimer,
                                    onActionChange = viewModel::updateSleepTimerAction
                                )
                            }

                            // Advanced Settings
                            item {
                                AdvancedSettingsSection(
                                    enableAnalytics = uiState.enableAnalytics,
                                    enableCrashReporting = uiState.enableCrashReporting,
                                    enableExperimentalFeatures = uiState.enableExperimentalFeatures,
                                    debugMode = uiState.debugMode,
                                    showDeveloperOptions = uiState.showDeveloperOptions,
                                    onToggleAnalytics = { /* TODO */ },
                                    onToggleCrashReporting = { /* TODO */ },
                                    onToggleExperimentalFeatures = { /* TODO */ },
                                    onToggleDebugMode = { /* TODO */ },
                                    onToggleDeveloperOptions = viewModel::toggleDeveloperOptions
                                )
                            }

                            // About Settings
                            item {
                                AboutSettingsSection(
                                    appVersion = uiState.appVersion,
                                    buildNumber = uiState.buildNumber,
                                    appSize = uiState.formattedAppSize,
                                    onViewChangelog = { /* TODO */ },
                                    onViewLicenses = { /* TODO */ },
                                    onContactSupport = { /* TODO */ },
                                    onVisitGitHub = { /* TODO */ },
                                    onNavigateToAbout = onNavigateToAbout
                                )
                            }

                            // Backup & Restore
                            item {
                                SettingsSectionHeader(
                                    title = "Backup & Restore",
                                    icon = Icons.Default.Backup
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = viewModel::toggleBackupDialog,
                                        modifier = Modifier.weight(1f),
                                        enabled = !uiState.isBackingUp
                                    ) {
                                        if (uiState.isBackingUp) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                        }
                                        Text("Backup")
                                    }
                                    
                                    OutlinedButton(
                                        onClick = viewModel::toggleRestoreDialog,
                                        modifier = Modifier.weight(1f),
                                        enabled = !uiState.isRestoring
                                    ) {
                                        if (uiState.isRestoring) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                        }
                                        Text("Restore")
                                    }
                                }
                            }

                            // Reset Settings
                            item {
                                SettingsSectionHeader(
                                    title = "Reset",
                                    icon = Icons.Default.RestartAlt
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = viewModel::toggleResetDialog,
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = !uiState.isResetting
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Reset Settings")
                                    }
                                    
                                    OutlinedButton(
                                        onClick = viewModel::toggleFactoryResetDialog,
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = !uiState.isResetting,
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Factory Reset")
                                    }
                                }
                            }

                            // Add bottom padding for mini player
                            item {
                                Spacer(modifier = Modifier.height(80.dp))
                            }
                        }
                    }
                }
            }
        }

        // Theme Selection Dialog
        if (uiState.showThemeSelectionDialog) {
            ThemeSelectionDialog(
                currentTheme = uiState.theme,
                availableThemes = uiState.availableThemes,
                onThemeSelected = viewModel::updateTheme,
                onDismiss = viewModel::toggleThemeSelectionDialog
            )
        }

        // Sleep Timer Dialog
        if (uiState.showSleepTimerDialog) {
            SleepTimerDialog(
                currentDuration = uiState.sleepTimerDuration,
                currentAction = uiState.sleepTimerAction,
                fadeOutDuration = uiState.fadeOutDuration,
                onStartTimer = { duration ->
                    viewModel.startSleepTimer(duration)
                },
                onActionChange = viewModel::updateSleepTimerAction,
                onDismiss = viewModel::toggleSleepTimerDialog
            )
        }

        // Error snackbar
        AnimatedVisibility(
            visible = uiState.hasError,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            ErrorSnackbar(
                error = uiState.currentError ?: "",
                onDismiss = viewModel::clearError,
                onRetry = viewModel::refreshSettings,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsTopBar(
    onNavigateBack: () -> Unit,
    onRefresh: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = "Settings",
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh"
                )
            }
        }
    )
}

@Composable
private fun SettingsLoadingState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                strokeWidth = 4.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading settings...",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsErrorState(
    error: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Failed to load settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(onClick = onDismiss) {
                    Text("Dismiss")
                }

                Button(onClick = onRetry) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Retry")
                }
            }
        }
    }
}

@Composable
private fun SleepTimerDialog(
    currentDuration: Int,
    currentAction: SleepTimerAction,
    fadeOutDuration: Int,
    onStartTimer: (Int) -> Unit,
    onActionChange: (SleepTimerAction) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedDuration by remember { mutableStateOf(currentDuration) }
    var selectedAction by remember { mutableStateOf(currentAction) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = null
            )
        },
        title = {
            Text("Sleep Timer")
        },
        text = {
            Column {
                Text("Set a timer to automatically pause playback")
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Duration selection
                Text(
                    text = "Duration: $selectedDuration minutes",
                    style = MaterialTheme.typography.titleSmall
                )
                
                Slider(
                    value = selectedDuration.toFloat(),
                    onValueChange = { selectedDuration = it.toInt() },
                    valueRange = 5f..120f,
                    steps = 22 // 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100, 105, 110, 115, 120
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Action selection
                Text(
                    text = "When timer ends:",
                    style = MaterialTheme.typography.titleSmall
                )
                
                SleepTimerAction.values().forEach { action ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedAction = action }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedAction == action,
                            onClick = { selectedAction = action }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(action.displayName)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onActionChange(selectedAction)
                    onStartTimer(selectedDuration)
                }
            ) {
                Text("Start Timer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ErrorSnackbar(
    error: String,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )

            TextButton(onClick = onRetry) {
                Text("Retry")
            }

            TextButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    }
}
