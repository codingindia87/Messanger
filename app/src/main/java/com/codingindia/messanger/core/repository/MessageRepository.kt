package com.codingindia.messanger.core.repository

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.workDataOf
import com.codingindia.messanger.R
import com.codingindia.messanger.core.dao.MessageDao
import com.codingindia.messanger.core.notification.models.NetworkMessage
import com.codingindia.messanger.core.notification.models.Notification
import com.codingindia.messanger.core.notification.models.NotificationData
import com.codingindia.messanger.core.utils.Conts
import com.codingindia.messanger.core.utils.CryptoHelper
import com.codingindia.messanger.core.workers.SendMessageWorker
import com.codingindia.messanger.core.workers.UploadWorker
import com.codingindia.messanger.features.home.domain.User
import com.codingindia.messanger.features.message.domain.Messages
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.concurrent.TimeUnit
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import kotlin.collections.plus

class MessageRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: FirebaseAuth,
    private val messageDao: MessageDao,
) {
    companion object {
        const val TAG = "MessageRepository"
    }

    suspend fun sendMessage(message: String, user: User, replyMessage: Messages?) {

        val timestamp = System.currentTimeMillis()

        val messageData = Messages(
            senderId = auth.uid!!,
            receiverId = user.id,
            messageContent = message,
            timestamp = timestamp,
            status = "sending",
            replyMessage = replyMessage?.messageContent
        )
        val messageId = messageDao.insertMessage(messageData)

        val notification = Notification(
            message = NotificationData(
                token = user.token, data = NetworkMessage(
                    title = "Chat Message",
                    body = "message",
                    senderUid = auth.uid,
                    textContent = CryptoHelper.encrypt(message),
                    timestamp = timestamp.toString(),
                    messageType = "text",
                    replyMessage = replyMessage?.messageContent,
                )
            )
        )

        val uniqueWorkName = "sendMessage_${messageId}"

        val notificationJson = Gson().toJson(notification)

        val workData = workDataOf(
            SendMessageWorker.KEY_MESSAGE_ID to messageId,
            SendMessageWorker.KEY_NOTIFICATION to notificationJson
        )

        val constraints =
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

        val messageSendRequest =
            OneTimeWorkRequestBuilder<SendMessageWorker>().setInputData(workData)
                .setConstraints(constraints).setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL, // हर बार वेटिंग टाइम बढ़ेगा (जैसे 10s, 20s, 40s...)
                    WorkRequest.MIN_BACKOFF_MILLIS, // कम से कम 10 सेकंड का गैप
                    TimeUnit.MILLISECONDS
                ).build()


        WorkManager.getInstance(context).enqueueUniqueWork(
            uniqueWorkName, ExistingWorkPolicy.APPEND, // Use APPEND_OR_REPLACE for robustness
            messageSendRequest
        )

        playMessageSentSound()
    }

    fun getConversationMessages(user1Id: String, user2Id: String): Flow<PagingData<Messages>> {
        return Pager(
            config = PagingConfig(
                pageSize = 30, // The number of items to load at once
                enablePlaceholders = false // Disable placeholders for a cleaner UI
            ), pagingSourceFactory = {
                messageDao.getConversationMessages(user1Id, user2Id)
            }).flow
    }

    suspend fun onMessageReceived(messages: Messages) {
        messageDao.insertMessage(messages)
    }

    suspend fun sendImage(uris: List<String>, user: User) {
        val placeholderText = if (uris.size > 1) "${uris.size} photos" else "Photo"

        val timestamp = System.currentTimeMillis()


        val messageData = Messages(
            senderId = auth.uid!!,
            receiverId = user.id,
            messageContent = placeholderText,
            timestamp = timestamp,
            status = "sending",
            messageType = "image",
            localFilePaths = uris,
            isRead = false
        )
        val messageId = messageDao.insertMessage(messageData)

        val constraints =
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()


        val uploadWorkRequests = OneTimeWorkRequestBuilder<UploadWorker>().setInputData(
            workDataOf(
                UploadWorker.KEY_URIS to uris.toTypedArray(), UploadWorker.MESSAGE_ID to messageId
            )
        ).setConstraints(constraints).build()

        val notification = Notification(
            message = NotificationData(
                token = user.token, data = NetworkMessage(
                    title = "Chat Message",
                    body = "message",
                    senderUid = auth.uid,
                    textContent = placeholderText,
                    timestamp = timestamp.toString(),
                    messageType = "image"
                )
            )
        )

        val notificationJson = Gson().toJson(notification)

        val sendMessageWorkRequest = OneTimeWorkRequestBuilder<SendMessageWorker>().setInputData(
            workDataOf(
                SendMessageWorker.KEY_MESSAGE_ID to messageId,
                SendMessageWorker.KEY_NOTIFICATION to notificationJson
            )
        ).build()

        WorkManager.getInstance(context)
            .beginWith(uploadWorkRequests) // Start with the parallel uploads
            .then(sendMessageWorkRequest)   // Run this after all uploads succeed
            .enqueue()

    }

    suspend fun sendAudio(uri: String, user: User) {
        val timestamp = System.currentTimeMillis()

        val messageData = Messages(
            senderId = auth.uid!!,
            receiverId = user.id,
            messageContent = "Audio",
            timestamp = timestamp,
            status = "sending",
            messageType = "audio",
            localFilePaths = listOf(uri),
            isRead = false
        )

        val messageId = messageDao.insertMessage(messageData)

        val constraints =
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()


        val uploadWorkRequests = OneTimeWorkRequestBuilder<UploadWorker>().setInputData(
            workDataOf(
                UploadWorker.KEY_URIS to listOf(uri).toTypedArray(),
                UploadWorker.MESSAGE_ID to messageId
            )
        ).setConstraints(constraints).build()

        val notification = Notification(
            message = NotificationData(
                token = user.token, data = NetworkMessage(
                    title = "Chat Message",
                    body = "message",
                    senderUid = auth.uid,
                    textContent = "Audio",
                    timestamp = timestamp.toString(),
                    messageType = "audio"
                )
            )
        )

        val notificationJson = Gson().toJson(notification)

        val sendMessageWorkRequest = OneTimeWorkRequestBuilder<SendMessageWorker>().setInputData(
            workDataOf(
                SendMessageWorker.KEY_MESSAGE_ID to messageId,
                SendMessageWorker.KEY_NOTIFICATION to notificationJson
            )
        ).build()

        WorkManager.getInstance(context)
            .beginWith(uploadWorkRequests) // Start with the parallel uploads
            .then(sendMessageWorkRequest)   // Run this after all uploads succeed
            .enqueue()

    }

    suspend fun markReceivedMessagesAsRead(currentUserId: String, senderId: String) {
        messageDao.markReceivedMessagesAsRead(currentUserId, senderId)
    }

    suspend fun markSendMessagesAsRead(currentUserId: String, senderId: String) {
        messageDao.markSendMessagesAsRead(currentUserId, senderId)
    }

    fun sendMessageSeen(user: User) {
        Log.d("MessageSeen", "Message seen send")
        val notification = Notification(
            message = NotificationData(
                token = user.token, data = NetworkMessage(
                    title = "Chat Message",
                    body = "seen-status",
                    senderUid = auth.uid,
                )
            )
        )

        val uniqueWorkName = System.currentTimeMillis()

        val notificationJson = Gson().toJson(notification)

        val workData = workDataOf(
            SendMessageWorker.KEY_MESSAGE_ID to uniqueWorkName,
            SendMessageWorker.KEY_NOTIFICATION to notificationJson
        )

        val constraints =
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

        val messageSendRequest =
            OneTimeWorkRequestBuilder<SendMessageWorker>().setInputData(workData)
                .setConstraints(constraints).build()


        WorkManager.getInstance(context).enqueueUniqueWork(
            uniqueWorkName.toString(),
            ExistingWorkPolicy.APPEND_OR_REPLACE, // Use APPEND_OR_REPLACE for robustness
            messageSendRequest
        )
    }

    // 1. Validate URLs exist
    suspend fun downloadImage(message: Messages) = withContext(Dispatchers.IO) {
        val urls = message.urls
        if (urls.isNullOrEmpty()) return@withContext

        val downloadedPaths = mutableListOf<String>()
        val successfulUrls = mutableListOf<String>()

        messageDao.updateIsDownloading(message.id, true)
        try {
            val receiveMediaDir = File(context.filesDir, "receive_media").apply {
                if (!exists()) {
                    mkdirs()
                    File(this, ".nomedia").createNewFile()
                }
            }

            urls.forEachIndexed { index, urlString ->
                try {
                    val url = URL(urlString)
                    val connection = url.openConnection()
                    connection.connect()

                    val fileLength = connection.contentLength
                    val inputStream = connection.getInputStream()
                    val outputStream = ByteArrayOutputStream()

                    val buffer = ByteArray(4096) // 4KB का बफर
                    var totalBytesRead = 0L
                    var bytesRead: Int

                    // 2. लूप में डेटा पढ़ना और प्रोग्रेस अपडेट करना
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        totalBytesRead += bytesRead
                        outputStream.write(buffer, 0, bytesRead)

                        // प्रोग्रेस कैलकुलेशन (जैसे 10, 25, 50...)
                        if (fileLength > 0) {
                            val progress = ((totalBytesRead * 100) / fileLength).toInt()
                            messageDao.updateProgress(message.id, progress)
                        }
                    }
                    // पूरी फाइल को बाइट्स में पढ़ें
                    val encryptedBytes = url.openStream().use { it.readBytes() }

                    // डिक्रिप्शन सेटअप (Upload वाले कोड से मैच होना चाहिए)
                    val keySpec = SecretKeySpec(Conts.SECRET_KEY.toByteArray(), "AES")
                    val ivSpec = IvParameterSpec(Conts.IV.toByteArray())
                    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
                    cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)

                    // असली इमेज बाइट्स प्राप्त करें
                    val decryptedBytes = cipher.doFinal(encryptedBytes)

                    // डिक्रिप्टेड फाइल को सेव करें
                    val finalFile = File(receiveMediaDir, "IMG_${System.currentTimeMillis()}.jpg")
                    FileOutputStream(finalFile).use { it.write(decryptedBytes) }

                    downloadedPaths.add(finalFile.absolutePath)
                } catch (e: Exception) {
                    Log.e("DownloadImage", "Decryption failed: ${e.message}")
                } finally {
                    messageDao.updateIsDownloading(message.id, false)
                }
            }

            // 5. डेटाबेस अपडेट
            if (downloadedPaths.isNotEmpty()) {
                val newPaths = (message.localFilePaths ?: emptyList()) + downloadedPaths
                messageDao.updateMessage(message.id, newPaths)
                successfulUrls.forEach { deleteFromFirebase(it) }
            }

        } catch (e: Exception) {
            Log.e("DownloadImage", "General Error: ${e.message}")
        }
    }

    private suspend fun deleteFromFirebase(url: String) {
        try {
            // Get reference directly from the download URL
            val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(url)

            // Delete and await completion
            storageRef.delete().await()

            Log.d("DeleteImage", "Successfully deleted image from Firebase: $url")

        } catch (e: Exception) {
            Log.e("DeleteImage", "Failed to delete from Firebase: ${e.message}")
        }
    }

    suspend fun sendReaction(messages: Messages, emoji: String, user: User) {

        val finalEmoji = if (messages.reaction == emoji) null else emoji

        messageDao.updateMessageReaction(messages.id, finalEmoji)

        if (auth.currentUser?.uid != user.id) {
            val notification = Notification(
                message = NotificationData(
                    token = user.token, data = NetworkMessage(
                        title = "Chat Message",
                        body = "message",
                        senderUid = messages.senderId,
                        receiverId = messages.receiverId,
                        timestamp = messages.timestamp.toString(),
                        messageType = "reaction",
                        reaction = finalEmoji ?: "removed",
                    )
                )
            )

            val uniqueWorkName = "sendMessage_${messages.id}"

            val notificationJson = Gson().toJson(notification)

            val workData = workDataOf(
                SendMessageWorker.KEY_MESSAGE_ID to messages.id,
                SendMessageWorker.KEY_NOTIFICATION to notificationJson
            )

            val constraints =
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

            val messageSendRequest =
                OneTimeWorkRequestBuilder<SendMessageWorker>().setInputData(workData)
                    .setConstraints(constraints).build()


            WorkManager.getInstance(context).enqueueUniqueWork(
                uniqueWorkName, ExistingWorkPolicy.APPEND, // Use APPEND_OR_REPLACE for robustness
                messageSendRequest
            )
        }
    }

    private fun playMessageSentSound() {
        try {
            // 1. अपनी raw फाइल का Path (URI) तैयार करें
            val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.send_tone}")

            // 2. RingtoneManager के जरिए रिंगटोन ऑब्जेक्ट प्राप्त करें
            val ringtone = RingtoneManager.getRingtone(context, soundUri)

            // 3. प्ले करें
            ringtone?.play()

        } catch (e: Exception) {
            e.printStackTrace()
            // बैकअप के लिए आप पुराना MediaPlayer वाला तरीका यहाँ रख सकते हैं
        }
    }

    suspend fun updateReactionByTimestamp(
        timestamp: Long, emoji: String?, senderId: String, receiverId: String
    ) {
        messageDao.updateReactionByTimestamp(timestamp, emoji, senderId, receiverId)
    }

    suspend fun downloadAudio(message: Messages) {
        // 1. डाउनलोड शुरू होने पर डेटाबेस में स्टेट अपडेट करें
        messageDao.updateIsDownloading(message.id, true)

        try {
            // डायरेक्टरी सेटअप
            val receiveMediaDir = File(context.filesDir, "receive_media").apply {
                if (!exists()) {
                    mkdirs()
                    File(this, ".nomedia").createNewFile()
                }
            }

            val downloadedPaths = mutableListOf<String>()

            message.urls?.forEach { urlString ->
                try {
                    val url = URL(urlString)
                    val connection = url.openConnection()
                    connection.connect()

                    val fileLength = connection.contentLength
                    val inputStream = connection.getInputStream()

                    // ऑडियो फाइल का नाम (टाइमस्टैम्प के साथ)
                    val outputFile = File(receiveMediaDir, "AUDIO_${System.currentTimeMillis()}.mp3")
                    val outputStream = FileOutputStream(outputFile)

                    val buffer = ByteArray(4096)
                    var bytesRead: Int
                    var totalBytesRead = 0L

                    // डेटा स्ट्रीम को फाइल में लिखें
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        totalBytesRead += bytesRead
                        outputStream.write(buffer, 0, bytesRead)

                        // प्रोग्रेस अपडेट (डेटाबेस में)
                        if (fileLength > 0) {
                            val progress = ((totalBytesRead * 100) / fileLength).toInt()
                            messageDao.updateProgress(message.id, progress)
                        }
                    }

                    outputStream.flush()
                    outputStream.close()
                    inputStream.close()

                    downloadedPaths.add(outputFile.absolutePath)

                } catch (e: Exception) {
                    Log.e("DownloadAudio", "Error downloading file: ${e.message}")
                }
            }

            // 2. डाउनलोड पूरा होने पर डेटाबेस अपडेट करें
            if (downloadedPaths.isNotEmpty()) {
                val newPaths = (message.localFilePaths ?: emptyList()) + downloadedPaths
                messageDao.updateMessage(message.id, newPaths)
                message.urls?.let { deleteFromFirebase(it[0]) }
                Log.d("DownloadAudio", "Audio downloaded successfully")
            }

        } catch (e: Exception) {
            Log.e("DownloadAudio", "General Error: ${e.message}")
        } finally {
            // 3. काम पूरा हो या एरर आए, 'isDownloading' को false जरूर करें
            messageDao.updateIsDownloading(message.id, false)
        }
    }
}
