package com.codingindia.messanger.core.notification.models

data class NotificationData(
    val token: String? = null,
    val data: NetworkMessage? = null,
)