package com.codingindia.messanger

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.work.Configuration
import androidx.work.DelegatingWorkerFactory
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.cloudinary.android.MediaManager
import com.codingindia.messanger.core.api.SendMessageApi
import com.codingindia.messanger.core.dao.MessageDao
import com.codingindia.messanger.core.workers.SendMessageWorker
import com.codingindia.messanger.core.workers.SendStatusWorker
import com.codingindia.messanger.core.workers.UploadWorker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class Messanger : Application(), Configuration.Provider {
    override fun onCreate() {
        super.onCreate()
        createChatChannel()

        val config = mapOf(
            "cloud_name" to "dybzqpt9q",
            "api_key" to "671622917655599",
            "api_secret" to "w-j5dM5zwkHXQ8CVPtZg3EyqPfU"
        )
        MediaManager.init(this, config)
    }

    @Inject
    lateinit var sendMessageWorkerFactory: SendMessageWorkerFactory

    @Inject
    lateinit var uploadImageWorkerFactory: UploadImageWorkerFactory

    @Inject
    lateinit var sendStatusWorkerFactory: SendStatusWorkerFactory


    override val workManagerConfiguration: Configuration
        get() {
            val delegatingWorkerFactory = DelegatingWorkerFactory()
            delegatingWorkerFactory.addFactory(uploadImageWorkerFactory)
            delegatingWorkerFactory.addFactory(sendMessageWorkerFactory)
            delegatingWorkerFactory.addFactory(sendStatusWorkerFactory)

            return Configuration.Builder().setMinimumLoggingLevel(Log.DEBUG)
                .setWorkerFactory(delegatingWorkerFactory).build()
        }

    class SendMessageWorkerFactory @Inject constructor(
        private val api: SendMessageApi, private val messageDao: MessageDao
    ) : WorkerFactory() {
        override fun createWorker(
            appContext: Context, workerClassName: String, workerParameters: WorkerParameters
        ): ListenableWorker? {
            return if (workerClassName == SendMessageWorker::class.java.name) {
                SendMessageWorker(appContext, workerParameters, api, messageDao)
            } else {
                null
            }
        }

    }

    class UploadImageWorkerFactory @Inject constructor(
        private val auth: FirebaseAuth,
        private val firebaseStorage: FirebaseStorage,
        private val messageDao: MessageDao
    ) : WorkerFactory() {
        override fun createWorker(
            appContext: Context, workerClassName: String, workerParameters: WorkerParameters
        ): ListenableWorker? {
            return if (workerClassName == UploadWorker::class.java.name) {
                UploadWorker(
                    appContext,
                    workerParameters,
                    auth = auth,
                    firebaseStorage = firebaseStorage,
                    messageDao
                )
            } else {
                // Return null so the DelegatingWorkerFactory can ask the next factory
                null
            }
        }
    }

    class SendStatusWorkerFactory @Inject constructor(
        private val database: FirebaseDatabase
    ) : WorkerFactory() {
        override fun createWorker(
            appContext: Context, workerClassName: String, workerParameters: WorkerParameters
        ): ListenableWorker? {
            return if (workerClassName == SendStatusWorker::class.java.name) {
                SendStatusWorker(
                    appContext, workerParameters, database = database
                )
            } else {
                // Return null so the DelegatingWorkerFactory can ask the next factory
                null
            }
        }

    }

    private fun createChatChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "chat_messages_channel"
            val channelName = "Chat messages"
            val channelDesc = "Notifications for chat messages"

            val importance =
                NotificationManager.IMPORTANCE_HIGH // heads‑up + sound/vibration [web:34][web:36][web:39]
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDesc
                enableVibration(true)
                vibrationPattern =
                    longArrayOf(0, 200, 150, 200) // custom pattern [web:31][web:32][web:37][web:43]
            }

            val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "CALL_CHANNEL_ID"
            val channelName = "Calling"
            val channelDesc = "Notifications for calling"

            val importance =
                NotificationManager.IMPORTANCE_HIGH // heads‑up + sound/vibration [web:34][web:36][web:39]
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDesc
                enableVibration(true)
                vibrationPattern =
                    longArrayOf(0, 200, 150, 200) // custom pattern [web:31][web:32][web:37][web:43]
            }

            val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

}