package com.codingindia.messanger.features.home.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codingindia.messanger.core.repository.UsersRepository
import com.codingindia.messanger.features.home.domain.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val userProfile: User) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

@HiltViewModel
class ProfileVIewModel @Inject constructor(
    private val usersRepository: UsersRepository
): ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating = _isUpdating.asStateFlow()

    init {
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading // Set loading state
            val result = usersRepository.fetchUserProfile() // Call the suspend function

            _uiState.value = when {
                result.isSuccess -> ProfileUiState.Success(result.getOrThrow())
                else -> ProfileUiState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun updateProfileImage(uri: Uri){
        viewModelScope.launch {
            val result = usersRepository.updateUserProfile(uri)
            if(result.isSuccess){
                fetchUserProfile()
            }
        }
    }

    fun updateUserName(newName: String){
        viewModelScope.launch {
            _isUpdating.value = true
            val updateResult = usersRepository.updateUserName(newName)
            if(updateResult.isSuccess){
                fetchUserProfile()
            }
            _isUpdating.value = false
        }
    }

}