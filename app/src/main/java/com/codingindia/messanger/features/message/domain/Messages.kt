package com.codingindia.messanger.features.message.domain

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "message")
data class Messages(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val senderId: String,
    val receiverId: String,
    val messageContent: String? = null,
    val replyMessage: String? = null,
    val replyId: Long? = 0,
    val timestamp: Long,
    val isRead: Boolean = false,
    val messageType: String = "text",
    val urls: List<String>? = null,
    val localFilePaths: List<String>? = null,
    var status: String? = null,// sending, send, failed
    val reaction: String? = null,
    val progress: Int = 0,
    val isDownloading: Boolean = false,
    val isUploading: Boolean = false
)
