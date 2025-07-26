package com.tinhtx.localplayerapplication.presentation.screens.settings

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tinhtx.localplayerapplication.domain.model.*
import com.tinhtx.localplayerapplication.presentation.components.common.*
import com.tinhtx.localplayerapplication.presentation.components.ui.MusicTopAppBar
import com.tinhtx.localplayerapplication.presentation.screens.settings.components.*
import com.tinhtx.localplayerapplication.presentation.theme.getHorizontalPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val listState = rememberLazyListState()

    var showThemeDialog by remember { mutableStateOf(false) }
    var showEqualizerDialog by remember { mutableStateOf(false) }
    var showStorageDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadSettings()
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MusicTopAppBar(
                title = "Settings",
                subtitle = "Customize your music experience",
                navigationIcon = Icons.Default.ArrowBack,
                onNavigationClick = onNavigateBack,
                scrollBehavior = scrollBehavior,
                actions = {
                    // Reset to defaults
                    IconButton(onClick = { viewModel.showResetDialog() }) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset to defaults"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    FullScreenLoadingIndicator(
                        message = "Loading settings...",
                        showBackground = false
                    )
                }
                uiState.error != null -> {
                    MusicErrorMessage(
                        title = "Unable to load settings",
                        message = uiState.error,
                        onRetry = { viewModel.retryLoadSettings() },
                        errorType = MusicErrorType.GENERAL,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                }
                else -> {
                    SettingsContent(
                        uiState = uiState,
                        windowSizeClass = windowSizeClass,
                        listState = listState,
                        onPlaybackSettingChanged = { setting, value ->
                            viewModel.updatePlaybackSetting(setting, value)
                        },
                        onLibrarySettingChanged = { setting, value ->
                            viewModel.updateLibrarySetting(setting, value)
                        },
                        onAppearanceSettingChanged = { setting, value ->
                            viewModel.updateAppearanceSetting(setting, value)
                        },
                        onNotificationSettingChanged = { setting, value ->
                            viewModel.updateNotificationSetting(setting, value)
                        },
                        onStorageSettingChanged = { setting, value ->
                            viewModel.updateStorageSetting(setting, value)
                        },
                        onShowThemeDialog = { showThemeDialog = true },
                        onShowEqualizerDialog = { showEqualizerDialog = true },
                        onShowStorageDialog = { showStorageDialog = true },
                        onShowAboutDialog = { showAboutDialog = true },
                        onScanLibrary = { viewModel.scanLibrary() },
                        onClearCache = { viewModel.clearCache() },
                        onExportSettings = { viewModel.exportSettings() },
                        onImportSettings = { viewModel.importSettings() }
                    )
                }
            }
        }
    }

    // Dialogs
    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = uiState.appearanceSettings.theme,
            onThemeSelected = { theme ->
                viewModel.updateAppearanceSetting(AppearanceSettingType.THEME, theme)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    if (showEqualizerDialog) {
        EqualizerSettingsDialog(
            equalizerSettings = uiState.playbackSettings.equalizerSettings,
            onSettingsChanged = { settings ->
                viewModel.updatePlaybackSetting(PlaybackSettingType.EQUALIZER, settings)
                showEqualizerDialog = false
            },
            onDismiss = { showEqualizerDialog = false }
        )
    }

    if (showStorageDialog) {
        StorageInfoDialog(
            storageInfo = uiState.storageInfo,
            onClearCache = { viewModel.clearCache() },
            onDismiss = { showStorageDialog = false }
        )
    }

    if (showAboutDialog) {
        AboutAppDialog(
            appInfo = uiState.appInfo,
            onDismiss = { showAboutDialog = false }
        )
    }

    if (uiState.showResetDialog) {
        ConfirmationDialog(
            title = "Reset Settings",
            message = "Are you sure you want to reset all settings to their default values? This action cannot be undone.",
            onConfirm = {
                viewModel.resetToDefaults()
            },
            onDismiss = { viewModel.hideResetDialog() },
            confirmText = "Reset",
            dismissText = "Cancel",
            isDestructive = true
        )
    }
}

@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    windowSizeClass: WindowSizeClass,
    listState: LazyListState,
    onPlaybackSettingChanged: (PlaybackSettingType, Any) -> Unit,
    onLibrarySettingChanged: (LibrarySettingType, Any) -> Unit,
    onAppearanceSettingChanged: (AppearanceSettingType, Any) -> Unit,
    onNotificationSettingChanged: (NotificationSettingType, Any) -> Unit,
    onStorageSettingChanged: (StorageSettingType, Any) -> Unit,
    onShowThemeDialog: () -> Unit,
    onShowEqualizerDialog: () -> Unit,
    onShowStorageDialog: () -> Unit,
    onShowAboutDialog: () -> Unit,
    onScanLibrary: () -> Unit,
    onClearCache: () -> Unit,
    onExportSettings: () -> Unit,
    onImportSettings: () -> Unit
) {
    val horizontalPadding = windowSizeClass.getHorizontalPadding()

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Playback Settings Section[1]
        item {
            SettingsSectionHeader(
                title = "Playback",
                subtitle = "Audio playback and quality settings",
                icon = Icons.Default.PlayArrow,
                modifier = Modifier.padding(horizontal = horizontalPadding)
            )
        }

        item {
            PlaybackSettingsSection(
                settings = uiState.playbackSettings,
                onSettingChanged = onPlaybackSettingChanged,
                onShowEqualizerDialog = onShowEqualizerDialog,
                modifier = Modifier.padding(horizontal = horizontalPadding)
            )
        }

        // Library Settings Section
        item {
            SettingsSectionHeader(
                title = "Music Library",
                subtitle = "Library scanning and organization",
                icon = Icons.Default.LibraryMusic,
                modifier = Modifier.padding(horizontal = horizontalPadding)
            )
        }

        item {
            LibrarySettingsSection(
                settings = uiState.librarySettings,
                onSettingChanged = onLibrarySettingChanged,
                onScanLibrary = onScanLibrary,
                modifier = Modifier.padding(horizontal = horizontalPadding)
            )
        }

        // Appearance Settings Section
        item {
            SettingsSectionHeader(
                title = "Appearance",
                subtitle = "Theme, colors, and display options",
                icon = Icons.Default.Palette,
                modifier = Modifier.padding(horizontal = horizontalPadding)
            )
        }

        item {
            AppearanceSettingsSection(
                settings = uiState.appearanceSettings,
                onSettingChanged = onAppearanceSettingChanged,
                onShowThemeDialog = onShowThemeDialog,
                modifier = Modifier.padding(horizontal = horizontalPadding)
            )
        }

        // Notification Settings Section
        item {
            SettingsSectionHeader(
                title = "Notifications",
                subtitle = "Media controls and notification settings",
                icon = Icons.Default.Notifications,
                modifier = Modifier.padding(horizontal = horizontalPadding)
            )
        }

        item {
            NotificationSettingsSection(
                settings = uiState.notificationSettings,
                onSettingChanged = onNotificationSettingChanged,
                modifier = Modifier.padding(horizontal = horizontalPadding)
            )
        }

        // Storage & Data Section
        item {
            SettingsSectionHeader(
                title = "Storage & Data",
                subtitle = "Cache, storage, and data management",
                icon = Icons.Default.Storage,
                modifier = Modifier.padding(horizontal = horizontalPadding)
            )
        }

        item {
            StorageSettingsSection(
                settings = uiState.storageSettings,
                storageInfo = uiState.storageInfo,
                onSettingChanged = onStorageSettingChanged,
                onShowStorageDialog = onShowStorageDialog,
                onClearCache = onClearCache,
                modifier = Modifier.padding(horizontal = horizontalPadding)
            )
        }

        // Advanced Settings Section
        item {
            SettingsSectionHeader(
                title = "Advanced",
                subtitle = "Developer options and advanced features",
                icon = Icons.Default.Settings,
                modifier = Modifier.padding(horizontal = horizontalPadding)
            )
        }

        item {
            AdvancedSettingsSection(
                onExportSettings = onExportSettings,
                onImportSettings = onImportSettings,
                modifier = Modifier.padding(horizontal = horizontalPadding)
            )
        }

        // About Section
        item {
            SettingsSectionHeader(
                title = "About",
                subtitle = "App information and legal",
                icon = Icons.Default.Info,
                modifier = Modifier.padding(horizontal = horizontalPadding)
            )
        }

        item {
            AboutSettingsSection(
                appInfo = uiState.appInfo,
                onShowAboutDialog = onShowAboutDialog,
                modifier = Modifier.padding(horizontal = horizontalPadding)
            )
        }
    }
}
