package com.tinhtx.localplayerapplication.presentation.screens.queue.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinhtx.localplayerapplication.domain.model.Song
import com.tinhtx.localplayerapplication.domain.usecase.music.GetAllSongsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddSongsViewModel @Inject constructor(
    private val getAllSongsUseCase: GetAllSongsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddSongsUiState())
    val uiState: StateFlow<AddSongsUiState> = _uiState.asStateFlow()

    fun loadAllSongs() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                getAllSongsUseCase().collect { songs ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null,
                        allSongs = songs
                    )
                }
            } catch (exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = exception.message ?: "Failed to load songs"
                )
            }
        }
    }
}

data class AddSongsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val allSongs: List<Song> = emptyList()
)
