package com.codingindia.messanger.features.home

import androidx.lifecycle.ViewModel
import com.codingindia.messanger.core.repository.PresenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PresenceViewModel @Inject constructor(
    private val presenceRepository: PresenceRepository
) : ViewModel() {

    fun onAppStart() {
        // First, configure the onDisconnect event
        presenceRepository.configureDisconnect()
        // Then, set the user's status to "online"
        presenceRepository.updateUserStatus(true)
    }

    fun onAppStop() {
        // Set the user's status to "offline"
        presenceRepository.updateUserStatus(false)
    }
}