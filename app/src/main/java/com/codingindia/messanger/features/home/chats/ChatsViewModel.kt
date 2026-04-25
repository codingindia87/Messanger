package com.codingindia.messanger.features.home.chats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codingindia.messanger.core.dao.ConversationDao
import com.codingindia.messanger.core.notification.receiver.PreferenceManager
import com.codingindia.messanger.features.home.domain.Conversation
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatsViewModel @Inject constructor(
    private val conversationDao: ConversationDao,
    private val auth: FirebaseAuth,
    private val preferenceManager: PreferenceManager
): ViewModel() {
    private val currentUserId = auth.currentUser?.uid!!

    val conversations: StateFlow<List<Conversation>> =
        conversationDao.getConversations(currentUserId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )


     fun deleteConversations(selectedIds: Set<String>){
        viewModelScope.launch {
            conversationDao.deleteConversations(currentUserId,selectedIds)
        }
    }

    fun getUnReadMessageCount(uid: String): Int{
        return preferenceManager.getUnread(uid)
    }
}