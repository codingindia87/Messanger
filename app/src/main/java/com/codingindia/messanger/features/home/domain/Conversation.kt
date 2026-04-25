package com.codingindia.messanger.features.home.domain

import androidx.room.Embedded
import com.codingindia.messanger.features.message.domain.Messages

data class Conversation(
    @Embedded
    val user: User,
    @Embedded(prefix = "msg_")
    val lastMessage: Messages,

)