package com.codingindia.messanger.features.updates.domain

data class AppUpdate(
    val title: String? = null,
    val description: String? = null,
    val version: Int? = null,
    val updates: List<String>? = null,
    val downloadUrl: String? = null,
    val size: String? = null
)
