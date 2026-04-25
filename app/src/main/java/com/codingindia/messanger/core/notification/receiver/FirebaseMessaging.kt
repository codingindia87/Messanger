package com.codingindia.messanger.core.notification.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.codingindia.messanger.MainActivity
import com.codingindia.messanger.R
import com.codingindia.messanger.core.broadcastreceivers.CallReceiver
import com.codingindia.messanger.core.repository.MessageRepository
import com.codingindia.messanger.core.utils.CryptoHelper
import com.codingindia.messanger.features.message.domain.Messages
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FirebaseMessaging : FirebaseMessagingService() {

    @Inject
    lateinit var messageRepository: MessageRepository

    @Inject
    lateinit var auth: FirebaseAuth

    @Inject
    lateinit var preferenceManager: PreferenceManager

    private var ringtone: Ringtone? = null


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        if (remoteMessage.data["body"] == "message") {
            Log.d("FirebaseMessagingTag", "Real message")

            val activeChatUserId = preferenceManager.getActiveChatUserId()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val listType = object : TypeToken<List<String>>() {}.type
                    remoteMessage.data.let { data ->

                        if (data["messageType"] == "reaction") {
                            val timestamp = data["timestamp"]?.toLong()

                            val reactionValue = data["reaction"]

                            val finalReaction: String? =
                                if (reactionValue == "removed" || reactionValue == null) {
                                    null
                                } else {
                                    reactionValue
                                }

                            val receiverId = data["receiverId"] ?: ""
                            val senderUid = data["senderUid"] ?: ""

                            messageRepository.updateReactionByTimestamp(
                                timestamp ?: 0L, finalReaction, senderUid, receiverId
                            )
                        } else {
                            val textMessage = CryptoHelper.decrypt(data["textContent"] ?: "")
                            val message = Messages(
                                senderId = data["senderUid"] ?: return@let,
                                receiverId = auth.uid!!,
                                messageContent = textMessage,
                                replyMessage = data["replyMessage"],
                                replyId = data["replyId"]?.toLong(),
                                timestamp = data["timestamp"]?.toLong()
                                    ?: System.currentTimeMillis(),
                                messageType = data["messageType"] ?: "text",
                                urls = Gson().fromJson(data["urls"], listType),
                                status = "received"
                            )
                            messageRepository.onMessageReceived(message)

                            if (data["senderUid"] != activeChatUserId) {
                                val senderUid = data["senderUid"]!!
                                val unreadForSender = preferenceManager.incrementUnread(senderUid)
                                sendNotification(
                                    "Messanger",
                                    "You have $unreadForSender unread messages",
                                    senderUid
                                )
                            } else {
                                playMessageReceivedSound(applicationContext)
                            }
                        }

                    }

                } catch (e: Exception) {
                    Log.e("FCM_TOKEN", "Failed to update token on the server.", e)
                }
            }
        }

        if (remoteMessage.data["body"] == "seen-status") {
            CoroutineScope(Dispatchers.IO).launch {
                messageRepository.markSendMessagesAsRead(
                    auth.uid!!, remoteMessage.data["senderUid"].toString()
                )
            }
        }

        if (remoteMessage.data["body"] == "VIDEO_CALL") {

            val type = remoteMessage.data["Type"]

            if (type == "OFFER_VIDEO_CALL") {
                // रिंगटोन सेटअप और प्ले करना
                val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                ringtone = RingtoneManager.getRingtone(applicationContext, ringtoneUri)
                ringtone?.play()

                showIncomingCallNotification(remoteMessage.data)
            } else if (type == "CANCEL_VIDEO_CALL") {
                // पुराने बज रहे रिंगटोन को रोकना
                ringtone?.stop()
                ringtone = null // मेमोरी क्लियर करने के लिए

                val notificationManager = getSystemService(NotificationManager::class.java)
                notificationManager.cancel(101)
            }
        }

    }


    fun showIncomingCallNotification(data: Map<String, String>) {
        val callerName = data["callerName"] ?: "Unknown Caller"

        // 1. Accept बटन के लिए Intent
        val acceptIntent = Intent(this, CallReceiver::class.java).apply {
            action = "ACTION_ANSWER"
        }
        val acceptPendingIntent =
            PendingIntent.getBroadcast(this, 0, acceptIntent, PendingIntent.FLAG_IMMUTABLE)

        // 2. Reject बटन के लिए Intent
        val rejectIntent = Intent(this, CallReceiver::class.java).apply {
            action = "ACTION_REJECT"
        }
        val rejectPendingIntent =
            PendingIntent.getBroadcast(this, 1, rejectIntent, PendingIntent.FLAG_IMMUTABLE)

        // 3. Notification बनाना
        val builder =
            NotificationCompat.Builder(this, "CALL_CHANNEL_ID").setSmallIcon(R.drawable.call_24px)
                .setContentTitle("Incoming Call").setContentText("$callerName is calling you")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .addAction(R.drawable.call_24px, "Accept", acceptPendingIntent)
                .addAction(R.drawable.call_end_24px, "Reject", rejectPendingIntent)
//            .setFullScreenIntent(fullScreenPendingIntent, true) // लॉक स्क्रीन पर भी दिखेगा
                .setOngoing(true) // यूजर इसे स्वाइप करके हटा न सके

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(101, builder.build())
    }


    private fun sendNotification(
        title: String?, body: String?, senderId: String
    ) {
        val channelId = "chat_messages_channel"

        val deepLinkUri = "app://chat/$senderId".toUri()
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(
            Intent.ACTION_VIEW, deepLinkUri, this, MainActivity::class.java
        )

        val pendingIntent = PendingIntent.getActivity(
            this,
            senderId.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
        )

        val soundUri = Uri.parse("android.resource://${applicationContext.packageName}/${R.raw.notification_tone}")

        val notificationBuilder =
            NotificationCompat.Builder(this, channelId).setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(title).setContentText(body).setContentIntent(pendingIntent)
                .setAutoCancel(true)
                // High priority for pre‑O devices [web:36][web:39][web:42][web:45]
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                // Optional: explicit vibration pattern for pre‑O [web:32][web:37][web:43][web:50]
                .setVibrate(longArrayOf(0, 200, 150, 200))
                .setSound(soundUri)

        notificationManager.notify(1, notificationBuilder.build())
    }


    private fun playMessageReceivedSound(context: Context) {
        try {
            // 1. अपनी raw फाइल का Path (URI) तैयार करें
            val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.received_tone}")

            // 2. RingtoneManager के जरिए रिंगटोन ऑब्जेक्ट प्राप्त करें
            val ringtone = RingtoneManager.getRingtone(context, soundUri)

            // 3. प्ले करें
            ringtone?.play()

        } catch (e: Exception) {
            e.printStackTrace()
            // बैकअप के लिए आप पुराना MediaPlayer वाला तरीका यहाँ रख सकते हैं
        }
    }
}