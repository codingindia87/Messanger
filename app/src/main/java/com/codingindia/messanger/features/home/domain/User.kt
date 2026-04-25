package com.codingindia.messanger.features.home.domain

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    var id: String,
    val name: String? = null,
    val imageUrl: String? = null,
    val token: String? = null,
    val online: Boolean? = false,
    val lastSeen: Long? = null,
    val chatRoom: String? = null,
    val typing: Boolean? = false
){
    constructor() : this("", "", "", "")
}