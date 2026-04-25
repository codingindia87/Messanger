package com.codingindia.messanger.core.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.codingindia.messanger.features.message.domain.Messages

@Dao
interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Messages): Long

    @Query("UPDATE message SET status = :newStatus WHERE id = :messageId")
    suspend fun updateMessageStatus(messageId: Long, newStatus: String)

    @Query("SELECT * FROM message WHERE (senderId = :user1Id AND receiverId = :user2Id) OR (senderId = :user2Id AND receiverId = :user1Id) ORDER BY timestamp DESC")//DESC
    fun getConversationMessages(
        user1Id: String, user2Id: String
    ): PagingSource<Int, Messages>

    @Query("UPDATE message SET isRead = 1 WHERE receiverId = :currentUserId AND senderId = :senderId AND isRead = 0")
    suspend fun markReceivedMessagesAsRead(currentUserId: String, senderId: String)

    @Query("UPDATE message SET isRead = 1 WHERE senderId = :currentUserId AND receiverId = :senderId AND isRead = 0")
    suspend fun markSendMessagesAsRead(currentUserId: String, senderId: String)

    @Query("UPDATE message SET localFilePaths = :newPaths WHERE id = :messageId")
    suspend fun updateMessage(messageId: Long, newPaths: List<String>)

    @Query("UPDATE message SET reaction = :emoji WHERE id = :messageId")
    suspend fun updateMessageReaction(messageId: Long, emoji: String?)

    @Query("UPDATE message SET reaction = :emoji WHERE timestamp = :timestamp AND senderId = :senderId AND receiverId = :receiverId")
    suspend fun updateReactionByTimestamp(
        timestamp: Long, emoji: String?, senderId: String, receiverId: String
    )

    // 1. सिर्फ Downloading Status अपडेट करने के लिए (True/False)
    @Query("UPDATE message SET isDownloading = :isDownloading WHERE id = :messageId")
    suspend fun updateIsDownloading(messageId: Long, isDownloading: Boolean)

    // 2. सिर्फ Progress अपडेट करने के लिए (0 to 100)
    @Query("UPDATE message SET progress = :progress WHERE id = :messageId")
    suspend fun updateProgress(messageId: Long, progress: Int)

    @Query("UPDATE message SET isUploading = :isUploading WHERE id = :messageId")
    suspend fun updateIsUploading(messageId: Long, isUploading: Boolean)

    // 3. दोनों को एक साथ अपडेट करने के लिए (Performance के लिए बेहतर)
    @Query("UPDATE message SET isDownloading = :isDownloading, progress = :progress WHERE id = :messageId")
    suspend fun updateDownloadState(messageId: Long, isDownloading: Boolean, progress: Int)

}