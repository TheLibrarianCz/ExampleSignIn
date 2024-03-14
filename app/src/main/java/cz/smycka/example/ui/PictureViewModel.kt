package cz.smycka.example.ui

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.smycka.example.domain.Base64Picture
import cz.smycka.example.domain.GetImageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "PictureViewModel"

@HiltViewModel
class PictureViewModel @Inject constructor(
    private val getImageUseCase: GetImageUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<PictureUiState>(PictureUiState.Loading)
    val uiState: StateFlow<PictureUiState> = _uiState.asStateFlow()

    val userName = savedStateHandle.getStateFlow(USERNAME_ARG_KEY, "")

    init {
        val userName = userName.value
        Log.d(TAG, "#init($userName)")
        viewModelScope.launch {
            _uiState.update {
                getImageUseCase(userName)?.let {
                    PictureUiState.Success(encodedPicture = it)
                } ?: PictureUiState.Error
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "#onCleared")
    }
}

sealed interface PictureUiState {
    data object Loading : PictureUiState
    data class Success(val encodedPicture: Base64Picture) : PictureUiState
    data object Error : PictureUiState
}
