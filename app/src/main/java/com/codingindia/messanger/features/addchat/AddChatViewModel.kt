package com.codingindia.messanger.features.addchat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codingindia.messanger.core.repository.UsersRepository
import com.codingindia.messanger.features.home.domain.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserListUiState(
    val users: List<User> = emptyList(), val isLoading: Boolean = true, val error: String? = null
)

@HiltViewModel
class AddChatViewModel @Inject constructor(
    private val userRepository: UsersRepository
) : ViewModel() {
    val allUsers: StateFlow<List<User>> = userRepository.getAllUsersFromRoom()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun refreshUsers() {
        viewModelScope.launch {
            userRepository.refreshUsersFromFirestore()
        }
    }
}