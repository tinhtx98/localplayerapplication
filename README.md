# **Cấu trúc Thư mục và File Hoàn chỉnh - LocalPlayer Application**

## **1. Cấu trúc Thư mục Đầy đủ**

```
LocalPlayerApplication/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/tinhtx/localplayerapplication/
│   │   │   │   ├── LocalPlayerApplication.kt
│   │   │   │   │
│   │   │   │   ├── core/
│   │   │   │   │   ├── constants/
│   │   │   │   │   │   ├── AppConstants.kt
│   │   │   │   │   │   ├── MediaConstants.kt
│   │   │   │   │   │   └── NotificationConstants.kt
│   │   │   │   │   ├── utils/
│   │   │   │   │   │   ├── Extensions.kt
│   │   │   │   │   │   ├── MediaUtils.kt
│   │   │   │   │   │   ├── ColorUtils.kt
│   │   │   │   │   │   ├── PermissionUtils.kt
│   │   │   │   │   │   ├── WindowSizeClassUtils.kt
│   │   │   │   │   │   ├── MediaStyleHelper.kt
│   │   │   │   │   │   └── NotificationChannelHelper.kt
│   │   │   │   │   └── di/
│   │   │   │   │       ├── AppModule.kt
│   │   │   │   │       ├── DatabaseModule.kt
│   │   │   │   │       ├── DataStoreModule.kt
│   │   │   │   │       ├── MediaModule.kt
│   │   │   │   │       ├── ImageModule.kt
│   │   │   │   │       └── RepositoryModule.kt
│   │   │   │   │
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/
│   │   │   │   │   │   ├── database/
│   │   │   │   │   │   │   ├── LocalPlayerDatabase.kt
│   │   │   │   │   │   │   ├── entities/
│   │   │   │   │   │   │   │   ├── SongEntity.kt
│   │   │   │   │   │   │   │   ├── AlbumEntity.kt
│   │   │   │   │   │   │   │   ├── ArtistEntity.kt
│   │   │   │   │   │   │   │   ├── PlaylistEntity.kt
│   │   │   │   │   │   │   │   ├── PlaylistSongCrossRef.kt
│   │   │   │   │   │   │   │   ├── HistoryEntity.kt
│   │   │   │   │   │   │   │   └── FavoriteEntity.kt
│   │   │   │   │   │   │   ├── dao/
│   │   │   │   │   │   │   │   ├── SongDao.kt
│   │   │   │   │   │   │   │   ├── AlbumDao.kt
│   │   │   │   │   │   │   │   ├── ArtistDao.kt
│   │   │   │   │   │   │   │   ├── PlaylistDao.kt
│   │   │   │   │   │   │   │   ├── HistoryDao.kt
│   │   │   │   │   │   │   │   └── FavoriteDao.kt
│   │   │   │   │   │   │   └── converters/
│   │   │   │   │   │   │       └── Converters.kt
│   │   │   │   │   │   ├── datastore/
│   │   │   │   │   │   │   ├── UserPreferences.kt
│   │   │   │   │   │   │   ├── PreferencesKeys.kt
│   │   │   │   │   │   │   └── PreferencesSerializer.kt
│   │   │   │   │   │   ├── media/
│   │   │   │   │   │   │   ├── MediaStoreScanner.kt
│   │   │   │   │   │   │   ├── AudioMetadataExtractor.kt
│   │   │   │   │   │   │   └── MediaScanner.kt
│   │   │   │   │   │   └── cache/
│   │   │   │   │   │       ├── ImageCacheManager.kt
│   │   │   │   │   │       └── AlbumArtCache.kt
│   │   │   │   │   └── repository/
│   │   │   │   │       ├── MusicRepositoryImpl.kt
│   │   │   │   │       ├── PlaylistRepositoryImpl.kt
│   │   │   │   │       ├── UserPreferencesRepositoryImpl.kt
│   │   │   │   │       └── MediaRepositoryImpl.kt
│   │   │   │   │
│   │   │   │   ├── domain/
│   │   │   │   │   ├── model/
│   │   │   │   │   │   ├── Song.kt
│   │   │   │   │   │   ├── Album.kt
│   │   │   │   │   │   ├── Artist.kt
│   │   │   │   │   │   ├── Playlist.kt
│   │   │   │   │   │   ├── UserProfile.kt
│   │   │   │   │   │   ├── PlaybackState.kt
│   │   │   │   │   │   ├── RepeatMode.kt
│   │   │   │   │   │   ├── ShuffleMode.kt
│   │   │   │   │   │   └── AudioFocusState.kt
│   │   │   │   │   ├── repository/
│   │   │   │   │   │   ├── MusicRepository.kt
│   │   │   │   │   │   ├── PlaylistRepository.kt
│   │   │   │   │   │   ├── UserPreferencesRepository.kt
│   │   │   │   │   │   └── MediaRepository.kt
│   │   │   │   │   └── usecase/
│   │   │   │   │       ├── music/
│   │   │   │   │       │   ├── GetAllSongsUseCase.kt
│   │   │   │   │       │   ├── GetSongsByAlbumUseCase.kt
│   │   │   │   │       │   ├── GetSongsByArtistUseCase.kt
│   │   │   │   │       │   ├── SearchSongsUseCase.kt
│   │   │   │   │       │   └── ScanMediaLibraryUseCase.kt
│   │   │   │   │       ├── playlist/
│   │   │   │   │       │   ├── GetPlaylistsUseCase.kt
│   │   │   │   │       │   ├── CreatePlaylistUseCase.kt
│   │   │   │   │       │   ├── DeletePlaylistUseCase.kt
│   │   │   │   │       │   ├── AddToPlaylistUseCase.kt
│   │   │   │   │       │   └── RemoveFromPlaylistUseCase.kt
│   │   │   │   │       ├── favorites/
│   │   │   │   │       │   ├── AddToFavoritesUseCase.kt
│   │   │   │   │       │   ├── RemoveFromFavoritesUseCase.kt
│   │   │   │   │       │   └── GetFavoritesUseCase.kt
│   │   │   │   │       ├── player/
│   │   │   │   │       │   ├── PlaySongUseCase.kt
│   │   │   │   │       │   ├── PauseSongUseCase.kt
│   │   │   │   │       │   ├── NextSongUseCase.kt
│   │   │   │   │       │   ├── PreviousSongUseCase.kt
│   │   │   │   │       │   ├── SeekToPositionUseCase.kt
│   │   │   │   │       │   ├── AudioFocusUseCase.kt
│   │   │   │   │       │   └── SleepTimerUseCase.kt
│   │   │   │   │       ├── user/
│   │   │   │   │       │   ├── UpdateUserProfileUseCase.kt
│   │   │   │   │       │   ├── GetUserProfileUseCase.kt
│   │   │   │   │       │   └── UpdateThemeUseCase.kt
│   │   │   │   │       └── cast/
│   │   │   │   │           └── CastMediaUseCase.kt
│   │   │   │   │
│   │   │   │   ├── presentation/
│   │   │   │   │   ├── MainActivity.kt
│   │   │   │   │   ├── navigation/
│   │   │   │   │   │   ├── LocalPlayerNavigation.kt
│   │   │   │   │   │   ├── NavDestinations.kt
│   │   │   │   │   │   ├── NavGraph.kt
│   │   │   │   │   │   └── BottomNavItem.kt
│   │   │   │   │   ├── theme/
│   │   │   │   │   │   ├── Color.kt
│   │   │   │   │   │   ├── Theme.kt
│   │   │   │   │   │   ├── Typography.kt
│   │   │   │   │   │   ├── Shape.kt
│   │   │   │   │   │   └── WindowSizeClass.kt
│   │   │   │   │   ├── components/
│   │   │   │   │   │   ├── common/
│   │   │   │   │   │   │   ├── LoadingIndicator.kt
│   │   │   │   │   │   │   ├── ErrorMessage.kt
│   │   │   │   │   │   │   ├── ResponsiveLayout.kt
│   │   │   │   │   │   │   ├── EmptyState.kt
│   │   │   │   │   │   │   └── CustomDialog.kt
│   │   │   │   │   │   ├── music/
│   │   │   │   │   │   │   ├── SongItem.kt
│   │   │   │   │   │   │   ├── AlbumCard.kt
│   │   │   │   │   │   │   ├── ArtistCard.kt
│   │   │   │   │   │   │   ├── PlayerControls.kt
│   │   │   │   │   │   │   ├── MiniPlayer.kt
│   │   │   │   │   │   │   ├── SeekBar.kt
│   │   │   │   │   │   │   └── ShuffleRepeatButtons.kt
│   │   │   │   │   │   ├── image/
│   │   │   │   │   │   │   ├── CoilAsyncImage.kt
│   │   │   │   │   │   │   ├── ImageLoader.kt
│   │   │   │   │   │   │   └── CircularAsyncImage.kt
│   │   │   │   │   │   ├── audio/
│   │   │   │   │   │   │   ├── AudioVisualizer.kt
│   │   │   │   │   │   │   └── WaveformView.kt
│   │   │   │   │   │   └── ui/
│   │   │   │   │   │       ├── CustomTopAppBar.kt
│   │   │   │   │   │       ├── PlayerBottomSheet.kt
│   │   │   │   │   │       ├── AdaptiveNavigation.kt
│   │   │   │   │   │       ├── NavigationRail.kt
│   │   │   │   │   │       └── BottomNavigationBar.kt
│   │   │   │   │   │
│   │   │   │   │   ├── screens/
│   │   │   │   │   │   ├── home/
│   │   │   │   │   │   │   ├── HomeScreen.kt
│   │   │   │   │   │   │   ├── HomeViewModel.kt
│   │   │   │   │   │   │   ├── HomeUiState.kt
│   │   │   │   │   │   │   └── components/
│   │   │   │   │   │   │       ├── UserProfileSection.kt
│   │   │   │   │   │   │       ├── QuickActionCards.kt
│   │   │   │   │   │   │       ├── MusicRecommendations.kt
│   │   │   │   │   │   │       ├── RecentlyPlayedSection.kt
│   │   │   │   │   │   │       └── TopAlbumsSection.kt
│   │   │   │   │   │   │
│   │   │   │   │   │   ├── player/
│   │   │   │   │   │   │   ├── PlayerScreen.kt
│   │   │   │   │   │   │   ├── PlayerViewModel.kt
│   │   │   │   │   │   │   ├── PlayerUiState.kt
│   │   │   │   │   │   │   └── components/
│   │   │   │   │   │   │       ├── PlayerPager.kt
│   │   │   │   │   │   │       ├── AlbumArtwork.kt
│   │   │   │   │   │   │       ├── SongInfo.kt
│   │   │   │   │   │   │       ├── ControlsSection.kt
│   │   │   │   │   │   │       ├── PlayerTopBar.kt
│   │   │   │   │   │   │       └── LyricsSection.kt
│   │   │   │   │   │   │
│   │   │   │   │   │   ├── library/
│   │   │   │   │   │   │   ├── LibraryScreen.kt
│   │   │   │   │   │   │   ├── LibraryViewModel.kt
│   │   │   │   │   │   │   ├── LibraryUiState.kt
│   │   │   │   │   │   │   └── tabs/
│   │   │   │   │   │   │       ├── SongsTab.kt
│   │   │   │   │   │   │       ├── ArtistsTab.kt
│   │   │   │   │   │   │       ├── AlbumsTab.kt
│   │   │   │   │   │   │       └── GenresTab.kt
│   │   │   │   │   │   │
│   │   │   │   │   │   ├── search/
│   │   │   │   │   │   │   ├── SearchScreen.kt
│   │   │   │   │   │   │   ├── SearchViewModel.kt
│   │   │   │   │   │   │   ├── SearchUiState.kt
│   │   │   │   │   │   │   └── components/
│   │   │   │   │   │   │       ├── SearchBar.kt
│   │   │   │   │   │   │       ├── SearchResults.kt
│   │   │   │   │   │   │       ├── SearchHistory.kt
│   │   │   │   │   │   │       └── SearchSuggestions.kt
│   │   │   │   │   │   │
│   │   │   │   │   │   ├── playlist/
│   │   │   │   │   │   │   ├── PlaylistsScreen.kt
│   │   │   │   │   │   │   ├── PlaylistDetailScreen.kt
│   │   │   │   │   │   │   ├── PlaylistViewModel.kt
│   │   │   │   │   │   │   ├── PlaylistUiState.kt
│   │   │   │   │   │   │   └── components/
│   │   │   │   │   │   │       ├── PlaylistCard.kt
│   │   │   │   │   │   │       ├── CreatePlaylistDialog.kt
│   │   │   │   │   │   │       ├── DraggableSongList.kt
│   │   │   │   │   │   │       └── PlaylistOptionsMenu.kt
│   │   │   │   │   │   │
│   │   │   │   │   │   ├── favorites/
│   │   │   │   │   │   │   ├── FavoritesScreen.kt
│   │   │   │   │   │   │   ├── FavoritesViewModel.kt
│   │   │   │   │   │   │   └── FavoritesUiState.kt
│   │   │   │   │   │   │
│   │   │   │   │   │   ├── queue/
│   │   │   │   │   │   │   ├── QueueScreen.kt
│   │   │   │   │   │   │   ├── QueueViewModel.kt
│   │   │   │   │   │   │   ├── QueueUiState.kt
│   │   │   │   │   │   │   └── components/
│   │   │   │   │   │   │       ├── QueueSongItem.kt
│   │   │   │   │   │   │       ├── DraggableQueueList.kt
│   │   │   │   │   │   │       └── QueueActions.kt
│   │   │   │   │   │   │
│   │   │   │   │   │   └── settings/
│   │   │   │   │   │       ├── SettingsScreen.kt
│   │   │   │   │   │       ├── SettingsViewModel.kt
│   │   │   │   │   │       ├── SettingsUiState.kt
│   │   │   │   │   │       └── components/
│   │   │   │   │   │           ├── ProfileSection.kt
│   │   │   │   │   │           ├── ThemeSelector.kt
│   │   │   │   │   │           ├── SettingsOptions.kt
│   │   │   │   │   │           ├── SleepTimerSettings.kt
│   │   │   │   │   │           └── AboutSection.kt
│   │   │   │   │   │
│   │   │   │   │   └── service/
│   │   │   │   │       ├── MusicService.kt
│   │   │   │   │       ├── MediaSessionManager.kt
│   │   │   │   │       ├── PlayerState.kt
│   │   │   │   │       ├── media/
│   │   │   │   │       │   ├── ExoPlayerManager.kt
│   │   │   │   │       │   ├── AudioFocusManager.kt
│   │   │   │   │       │   ├── MediaSessionCallback.kt
│   │   │   │   │       │   └── MediaPlaybackPreparer.kt
│   │   │   │   │       ├── cast/
│   │   │   │   │       │   ├── CastManager.kt
│   │   │   │   │       │   └── CastOptionsProvider.kt
│   │   │   │   │       ├── timer/
│   │   │   │   │       │   ├── SleepTimerManager.kt
│   │   │   │   │       │   └── SleepTimerService.kt
│   │   │   │   │       └── notifications/
│   │   │   │   │           ├── MusicNotificationManager.kt
│   │   │   │   │           ├── MediaStyleNotification.kt
│   │   │   │   │           └── NotificationHelper.kt
│   │   │   │   │
│   │   │   │   └── shared/
│   │   │   │       ├── state/
│   │   │   │       │   ├── UiState.kt
│   │   │   │       │   ├── Resource.kt
│   │   │   │       │   └── LoadingState.kt
│   │   │   │       └── extension/
│   │   │   │           ├── ContextExtensions.kt
│   │   │   │           ├── ComposeExtensions.kt
│   │   │   │           ├── MediaExtensions.kt
│   │   │   │           └── StringExtensions.kt
│   │   │   │
│   │   │   ├── res/
│   │   │   │   ├── drawable/
│   │   │   │   │   ├── ic_launcher_foreground.xml
│   │   │   │   │   ├── ic_music_note.xml
│   │   │   │   │   ├── ic_default_album.xml
│   │   │   │   │   └── rounded_background.xml
│   │   │   │   ├── drawable-v24/
│   │   │   │   │   └── ic_launcher_foreground.xml
│   │   │   │   ├── mipmap-*/
│   │   │   │   │   ├── ic_launcher.webp
│   │   │   │   │   └── ic_launcher_round.webp
│   │   │   │   ├── values/
│   │   │   │   │   ├── colors.xml
│   │   │   │   │   ├── strings.xml
│   │   │   │   │   ├── themes.xml
│   │   │   │   │   └── dimens.xml
│   │   │   │   ├── values-night/
│   │   │   │   │   ├── colors.xml
│   │   │   │   │   └── themes.xml
│   │   │   │   ├── values-v31/
│   │   │   │   │   └── themes.xml
│   │   │   │   └── xml/
│   │   │   │       ├── backup_rules.xml
│   │   │   │       └── data_extraction_rules.xml
│   │   │   │
│   │   │   └── AndroidManifest.xml
│   │   │
│   │   ├── androidTest/java/com/tinhtx/localplayerapplication/
│   │   │   ├── ExampleInstrumentedTest.kt
│   │   │   ├── database/
│   │   │   │   └── DatabaseTest.kt
│   │   │   └── ui/
│   │   │       └── MainActivityTest.kt
│   │   │
│   │   └── test/java/com/tinhtx/localplayerapplication/
│   │       ├── ExampleUnitTest.kt
│   │       ├── domain/
│   │       │   └── usecase/
│   │       │       └── GetAllSongsUseCaseTest.kt
│   │       └── data/
│   │           └── repository/
│   │               └── MusicRepositoryTest.kt
│   │
│   ├── build.gradle.kts
│   └── proguard-rules.pro
│
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
└── .gitignore
```

## **2. File build.gradle.kts (Project Level)**

```kotlin
plugins {
    id("com.android.application") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false
    id("com.google.devtools.ksp") version "1.9.22-1.0.17" apply false
}
```

## **3. File build.gradle.kts (App Level)**

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
    id("kotlin-parcelize")
}

android {
    namespace = "com.tinhtx.localplayerapplication"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.tinhtx.localplayerapplication"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    
    kotlinOptions {
        jvmTarget = "1.8"
    }
    
    buildFeatures {
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    
    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    
    // Window Size Class
    implementation("androidx.compose.material3:material3-window-size-class")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.6")
    
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    
    // Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-android-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    
    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    
    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // Media3
    implementation("androidx.media3:media3-exoplayer:1.2.1")
    implementation("androidx.media3:media3-ui:1.2.1")
    implementation("androidx.media3:media3-session:1.2.1")
    
    // Coil for Image Loading
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("io.coil-kt:coil-gif:2.5.0")
    
    // Cast Support
    implementation("com.google.android.gms:play-services-cast-framework:21.4.0")
    
    // Audio Visualizer
    implementation("com.github.gauravk95:audio-visualizer-android:0.9.2")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // Permissions
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.8.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("androidx.room:room-testing:2.6.1")
    
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.02.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

kapt {
    correctErrorTypes = true
}
```

## **4. AndroidManifest.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <!-- Media và Storage Permissions -->
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
        android:maxSdkVersion="32" />
    <uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />
    
    <!-- Cast Permission -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />

    <application
        android:name=".LocalPlayerApplication"
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.LocalPlayerApplication"
        tools:targetApi="31">
        
        <!-- Main Activity -->
        <activity
            android:name=".presentation.MainActivity"
            android:exported="true"
            android:theme="@style/Theme.LocalPlayerApplication">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- Music Service -->
        <service
            android:name=".presentation.service.MusicService"
            android:exported="false"
            android:foregroundServiceType="mediaPlayback">
            <intent-filter>
                <action android:name="androidx.media3.session.MediaSessionService" />
            </intent-filter>
        </service>

        <!-- Sleep Timer Service -->
        <service
            android:name=".presentation.service.timer.SleepTimerService"
            android:exported="false" />

        <!-- Cast Options Provider -->
        <meta-data
            android:name="com.google.android.gms.cast.framework.OPTIONS_PROVIDER_CLASS_NAME"
            android:value=".presentation.service.cast.CastOptionsProvider" />

    </application>

</manifest>
```

## **5. settings.gradle.kts**

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "LocalPlayerApplication"
include(":app")
```

## **6. gradle.properties**

```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
```