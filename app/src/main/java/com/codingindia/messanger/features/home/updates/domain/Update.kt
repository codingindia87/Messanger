package com.codingindia.messanger.features.home.updates.domain

data class Update(
    var id: String? = null,
    val uid: String? = null,
    val userName: String? = null,
    var url: List<String>? = null,
    var timeAgo: Long? = null,
    val userImage: String? = null,
    val captions: String? = null
)
