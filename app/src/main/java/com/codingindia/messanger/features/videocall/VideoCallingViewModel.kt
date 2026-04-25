package com.codingindia.messanger.features.videocall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codingindia.messanger.core.repository.VideoCallRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideoCallingViewModel @Inject constructor(
    private val videoCallRepository: VideoCallRepository
) : ViewModel() {

    fun startCall(name: String, token: String, uid: String) {
        viewModelScope.launch {
            videoCallRepository.startCall(token, uid, name)
        }
    }

    fun endCall(token: String, uid: String) {
        viewModelScope.launch {
            videoCallRepository.endCall(token, uid)
        }
    }

    fun toggleMic() {

    }

    fun toggleCamera() {

    }

    fun toggleSpeaker() {

    }

    fun toggleVideo() {

    }
}