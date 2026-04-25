package com.codingindia.messanger.core.notification.models

data class NetworkMessage(
    var title: String,
    val body: String,
    val senderUid: String? = null,
    val receiverId: String? = null,
    val textContent: String? = null,
    val timestamp: String? = null,
    val messageType: String? = null, //image,text,video
    val replyMessage: String? = "",
    val replyId: String? = "0",
    var urls: String? = null,
    val reaction: String? = null,
)