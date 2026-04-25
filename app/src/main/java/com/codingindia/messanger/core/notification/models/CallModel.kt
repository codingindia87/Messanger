package com.codingindia.messanger.core.notification.models

data class CallNotification(
    val message: Call
)

data class Call(
    val token: String,
    val data: MutableMap<String, String>
)
