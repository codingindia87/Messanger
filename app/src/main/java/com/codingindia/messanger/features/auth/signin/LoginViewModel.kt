package com.codingindia.messanger.features.auth.signin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codingindia.messanger.core.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val email: String = "", val password: String = "", val isLoading: Boolean = false
)

sealed class LoginEvent {
    data class EmailChanged(val email: String) : LoginEvent()
    data class PasswordChanged(val password: String) : LoginEvent()
    data object Login : LoginEvent()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {
    var uiState by mutableStateOf(LoginUiState())
        private set

    private val _loginResult = MutableSharedFlow<Result<Unit>>()
    val loginResult = _loginResult.asSharedFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> uiState = uiState.copy(email = event.email)
            is LoginEvent.PasswordChanged -> uiState = uiState.copy(password = event.password)
            is LoginEvent.Login -> loginUser()
        }
    }

    private fun loginUser() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            val result = repository.signIn(uiState.email, uiState.password)
            _loginResult.emit(result)
            uiState = uiState.copy(isLoading = false)
        }
    }

}