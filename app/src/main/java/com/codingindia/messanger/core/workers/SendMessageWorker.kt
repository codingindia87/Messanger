package com.codingindia.messanger.core.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.codingindia.messanger.core.api.SendMessageApi
import com.codingindia.messanger.core.dao.MessageDao
import com.codingindia.messanger.core.notification.models.Notification
import com.google.gson.Gson
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import retrofit2.HttpException

@HiltWorker
class SendMessageWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    @Assisted private val api: SendMessageApi,
    @Assisted private val messageDao: MessageDao,
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val KEY_MESSAGE_ID = "message_id"
        const val KEY_NOTIFICATION = "notification_json"
    }

    override suspend fun doWork(): Result {

        val messageId = inputData.getLong(KEY_MESSAGE_ID, -1L)
        val notificationJson = inputData.getString(KEY_NOTIFICATION)

        val downloadUrls = inputData.getStringArray(UploadWorker.KEY_DOWNLOAD_URL)

        Log.d("SendMessageWorker", downloadUrls.toString())

        if (messageId == -1L || notificationJson == null) {
            return Result.failure()
        }

        val originalNotification: Notification =
            Gson().fromJson(notificationJson, Notification::class.java)

        Log.d("SendMessageWorker", Gson().toJson(downloadUrls?.toList()))

        originalNotification.message?.data?.urls = Gson().toJson(downloadUrls?.toList())

        return try {
            Log.d("SendMessageWorker", originalNotification.toString())
            val response = api.sendMessage(originalNotification)
            if (response.isSuccessful) {
                messageDao.updateMessageStatus(messageId, "send")
                Result.success()
            } else {
                messageDao.updateMessageStatus(messageId, "failed")
                Result.retry()
            }
        } catch (e: HttpException) {
            messageDao.updateMessageStatus(messageId, "failed")
            Result.retry()
        } catch (e: Exception) {
            messageDao.updateMessageStatus(messageId, "failed")
            Result.retry()
        } finally {
            if (originalNotification.message?.data?.messageType == "image") {
                messageDao.updateIsUploading(messageId, false)
            }
        }
    }


}
