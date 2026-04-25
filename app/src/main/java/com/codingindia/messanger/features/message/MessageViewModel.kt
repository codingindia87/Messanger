package com.codingindia.messanger.features.message

import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.codingindia.messanger.core.notification.receiver.PreferenceManager
import com.codingindia.messanger.core.repository.MessageRepository
import com.codingindia.messanger.core.repository.UsersRepository
import com.codingindia.messanger.features.home.domain.User
import com.codingindia.messanger.features.message.domain.Messages
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessageViewModel @Inject constructor(
    private val usersRepository: UsersRepository,
    savedStateHandle: SavedStateHandle,
    private val messageRepository: MessageRepository,
    val preferenceManager: PreferenceManager,
    val auth: FirebaseAuth,
) : ViewModel() {

    private val userId: String = checkNotNull(savedStateHandle["uid"])

    private var mediaPlayer: MediaPlayer? = null

    init {
        viewModelScope.launch {
            messageRepository.markReceivedMessagesAsRead(auth.currentUser?.uid!!, userId)
        }
        sendMessageSeen()
        usersRepository.startListeningForStatusUpdates(userId)
        preferenceManager.resetUnread(userId)
    }

    val user: StateFlow<User?> = usersRepository.getUserById(userId).stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = null
    )

    fun sendMessage(message: String, replyMessage: Messages?, user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            messageRepository.sendMessage(message, user, replyMessage)
        }
    }

    fun getMessages(user1Id: String, user2Id: String): Flow<PagingData<Messages>> {
        return messageRepository.getConversationMessages(user1Id, user2Id).cachedIn(viewModelScope)
    }

    fun sendImage(uris: List<Uri>, user: User) {
        viewModelScope.launch {
            messageRepository.sendImage(uris.map { it.toString() }, user)
        }
    }

    fun sendAudio(uri: Uri, user: User){
        viewModelScope.launch {
            messageRepository.sendAudio(uri.toString(), user)
        }
    }

    fun updateChatRoom(roomId: String?) {
        usersRepository.updateChatRoom(roomId)
    }

    fun setTyping(isTyping: Boolean) {
        usersRepository.setTyping(isTyping)
    }

    fun sendMessageSeen(){
        viewModelScope.launch {
            usersRepository.getUserById(userId).collect { userFromDb->
                Log.d("MessageSeen", "UsrId ${userFromDb?.token}")
                userFromDb?.let { messageRepository.sendMessageSeen(it) }
            }
        }
    }

    fun downloadImage(messages: Messages){
        viewModelScope.launch {
            messageRepository.downloadImage(messages)
        }
    }

    fun sendReaction(messages: Messages, emoji: String, user: User){
        viewModelScope.launch {
            messageRepository.sendReaction(messages,emoji,user)
        }
    }


    fun playAudio(path: String) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(path)
            prepare()
            start()
        }
    }

    fun downloadAudio(messages: Messages){
        viewModelScope.launch {
            messageRepository.downloadAudio(messages)
        }
    }

    override fun onCleared() {
        super.onCleared()
        usersRepository.stopListeningForStatusUpdates()
    }
}