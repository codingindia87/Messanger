package com.codingindia.messanger.core.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.codingindia.messanger.features.home.domain.Conversation
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Transaction
    @Query(
        """
        SELECT 
            u.*,
            m.id AS msg_id,
            m.senderId AS msg_senderId,
            m.receiverId AS msg_receiverId,
            m.messageContent AS msg_messageContent,
            m.replyMessage AS msg_replyMessage,
            m.replyId AS msg_replyId,
            m.timestamp AS msg_timestamp,
            m.isRead AS msg_isRead,
            m.messageType AS msg_messageType,
            m.urls AS msg_url,
            m.localFilePaths AS msg_localFilePath,
            m.status AS msg_status,
            m.progress AS msg_progress,           -- 👈 यह जोड़ें
        m.isDownloading AS msg_isDownloading, -- 👈 यह जोड़ें
        m.isUploading AS msg_isUploading      -- 👈 यह जोड़ें
        FROM 
            message m
        INNER JOIN (
            SELECT
                CASE
                    WHEN senderId = :currentUserId THEN receiverId
                    ELSE senderId
                END AS otherUserId,
                MAX(m.timestamp) AS max_timestamp
            FROM 
                message m
            WHERE 
                m.senderId = :currentUserId OR m.receiverId = :currentUserId
            GROUP BY 
                otherUserId
        ) AS last_message_info ON 
            (
                (m.senderId = :currentUserId AND m.receiverId = last_message_info.otherUserId) OR
                (m.senderId = last_message_info.otherUserId AND m.receiverId = :currentUserId)
            ) AND m.timestamp = last_message_info.max_timestamp
        INNER JOIN 
            users u ON u.id = last_message_info.otherUserId
        ORDER BY 
            m.timestamp DESC
    """
    )
    fun getConversations(currentUserId: String): Flow<List<Conversation>>

    @Query(
        """
        DELETE FROM message 
        WHERE (senderId = :currentUserId AND receiverId IN (:otherUserIds)) 
           OR (receiverId = :currentUserId AND senderId IN (:otherUserIds))
    """
    )
    suspend fun deleteConversations(currentUserId: String, otherUserIds: Set<String>)
}
