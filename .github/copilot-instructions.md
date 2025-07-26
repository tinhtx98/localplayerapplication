# Copilot Instructions for LocalPlayerApplication

## Project Overview
- **Type:** Android music player app using Kotlin, Jetpack Compose, Hilt, Room, DataStore, and Media3.
- **Structure:** Follows a clean architecture with clear separation between `data`, `domain`, and `presentation` layers under `app/src/main/java/com/tinhtx/localplayerapplication/`.
- **Major Components:**
  - **Data Layer:** Handles local database (Room), DataStore, media scanning, and caching.
  - **Domain Layer:** Contains models, repository interfaces, and use cases grouped by feature (music, playlist, favorites, player, user, cast).
  - **Presentation Layer:** UI screens, components, navigation, themes, and service classes for playback, casting, and notifications.

## Key Patterns & Conventions
- **Use Cases:** All business logic is encapsulated in use case classes under `domain/usecase/` (e.g., `PlaySongUseCase.kt`).
- **Repositories:** Interfaces in `domain/repository/`, implemented in `data/repository/`.
- **Dependency Injection:** Uses Hilt modules in `core/di/` for providing dependencies.
- **UI:** Built with Jetpack Compose; theming in `presentation/theme/`, navigation in `presentation/navigation/`.
- **Testing:**
  - Unit tests in `test/java/com/tinhtx/localplayerapplication/`
  - Instrumented tests in `androidTest/java/com/tinhtx/localplayerapplication/`

## Developer Workflows
- **Build:**
  - Standard Gradle: `./gradlew build`
  - Clean: `./gradlew clean`
- **Run:**
  - Use Android Studio or `./gradlew installDebug` for device/emulator
- **Test:**
  - Unit: `./gradlew test`
  - Instrumented: `./gradlew connectedAndroidTest`
- **Code Generation:**
  - KSP and kapt are used for Room and Hilt; ensure to sync Gradle after dependency changes.

## Integration Points
- **Media Playback:** Uses `androidx.media3` (ExoPlayer) for audio, with service in `presentation/service/`.
- **Casting:** Google Cast via `play-services-cast-framework` and custom options provider.
- **Image Loading:** Coil for Compose.
- **Audio Visualizer:** External library for waveform/visual effects.

## Project-Specific Notes
- **Permissions:** Handles Android 13+ media permissions and legacy storage permissions in `AndroidManifest.xml`.
- **Resource Organization:** UI components are grouped by feature and type (e.g., `components/music/`, `screens/player/`).
- **Theming:** Custom themes and window size classes for responsive design.
- **Navigation:** Centralized in `presentation/navigation/`.

## Examples
- To add a new feature, create use cases in `domain/usecase/feature/`, update repository interfaces, and implement in `data/repository/`.
- For new screens, add to `presentation/screens/feature/` and update navigation.

Refer to `README.md` for a full directory map and more details on dependencies and configuration.
