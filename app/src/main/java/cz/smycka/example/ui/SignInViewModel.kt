package cz.smycka.example.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cz.smycka.example.domain.SignInResult
import cz.smycka.example.domain.SignInUseCase
import cz.smycka.example.network.ConnectionObserver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val connectionObserver: ConnectionObserver,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _signInEventsWithChannel = Channel<SignInEvents>()
    val signInEvents = _signInEventsWithChannel.receiveAsFlow()

    private val _signInUiState = MutableStateFlow<SignInUiState>(SignInUiState.None)
    val signInUiState: StateFlow<SignInUiState> = _signInUiState.asStateFlow()

    val userName = savedStateHandle.getStateFlow(key = USERNAME, initialValue = "")
    val password = savedStateHandle.getStateFlow(key = PASSWORD, initialValue = "")

    init {
        viewModelScope.launch {
            connectionObserver.isConnected.collect {
                if (_signInUiState.value is SignInUiState.NoInternet) {
                    _signInUiState.update { SignInUiState.None }
                }
            }
        }
    }

    fun onUserNameChange(userName: String) {
        savedStateHandle[USERNAME] = userName
        _signInUiState.update { SignInUiState.None }
    }

    fun onPasswordChange(password: String) {
        savedStateHandle[PASSWORD] = password
        _signInUiState.update { SignInUiState.None }
    }

    /**
     * Signs in with [userName] with [password].
     *
     * @throws IllegalStateException [userName], can't be empty nor have upper cased characters. [password] Can't be empty.
     */
    fun signIn() {
        val (userName, password) = userName.value to password.value
        check(userName.isNotEmpty() && userName.none { it.isUpperCase() }) {
            "User name can't have upper case characters or be empty."
        }
        check(userName.isNotEmpty() && password.none { it.isUpperCase() }) {
            "Password name can't have upper case characters or be empty."
        }
        signIn(userName, password)
    }

    private fun signIn(userName: String, password: String) {
        if (connectionObserver.isCurrentlyConnected) {
            _signInUiState.update { SignInUiState.Loading }
        } else {
            _signInUiState.update { SignInUiState.NoInternet }
            return
        }
        viewModelScope.launch {
            when (signInUseCase(userName, password)) {
                SignInResult.Error -> {
                    _signInUiState.update { SignInUiState.Error }
                }

                SignInResult.Success -> {
                    _signInUiState.update { SignInUiState.Success }
                    delay(300L)
                    _signInEventsWithChannel.send(SignInEvents.NavigateToPictureScreen(userName))
                }

                SignInResult.Unauthorized -> {
                    _signInUiState.update { SignInUiState.WrongUserNameOrPassword }
                }
            }
        }
    }

    fun clearResult() {
        _signInUiState.update { SignInUiState.None }
        savedStateHandle[USERNAME] = ""
        savedStateHandle[PASSWORD] = ""
    }
}

sealed class SignInEvents {
    data class NavigateToPictureScreen(val userName: String) : SignInEvents()
}

sealed interface SignInUiState {
    data object None : SignInUiState
    data object Loading : SignInUiState
    data object Success : SignInUiState
    data object Error : SignInUiState
    data object NoInternet : SignInUiState
    data object WrongUserNameOrPassword : SignInUiState
}

private const val USERNAME = "userName"
private const val PASSWORD = "password"
