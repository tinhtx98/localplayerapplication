package com.tinhtx.localplayerapplication.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinhtx.localplayerapplication.core.constants.AppConstants
import com.tinhtx.localplayerapplication.domain.usecase.settings.GetAppSettingsUseCase
import com.tinhtx.localplayerapplication.domain.usecase.settings.UpdateSettingsUseCase
import com.tinhtx.localplayerapplication.domain.usecase.user.UpdateThemeUseCase
import com.tinhtx.localplayerapplication.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getAppSettingsUseCase: GetAppSettingsUseCase,
    private val updateSettingsUseCase: UpdateSettingsUseCase,
    private val updateThemeUseCase: UpdateThemeUseCase // Existing UseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                getAppSettingsUseCase.flow().collect { settings ->
                    _uiState.value = _uiState.value.copy(
                        appSettings = settings,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = exception.message ?: "Failed to load settings"
                )
            }
        }
    }

    fun updateTheme(theme: AppConstants.ThemeMode) {
        viewModelScope.launch {
            try {
                updateThemeUseCase(theme)
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = exception.message ?: "Failed to update theme"
                )
            }
        }
    }

    fun updatePlaybackSettings(settings: PlaybackSettings) {
        viewModelScope.launch {
            try {
                updateSettingsUseCase.updatePlaybackSettings(settings)
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = exception.message ?: "Failed to update playback settings"
                )
            }
        }
    }

    fun updateAppearanceSettings(settings: AppearanceSettings) {
        viewModelScope.launch {
            try {
                updateSettingsUseCase.updateAppearanceSettings(settings)
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = exception.message ?: "Failed to update appearance settings"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
