package com.codingindia.messanger.core.broadcastreceivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class CallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action

        // नोटिफिकेशन ID जिसे हटाना है (आपके पिछले कोड के अनुसार 101)
        val notificationId = 101
        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (action == "ACTION_REJECT") {
            // 1. रिंगटोन बंद करें (अगर आपने इसे Singleton या Service में रखा है)
            // RingtoneManager.stop()

            // 2. नोटिफिकेशन हटाएं
            notificationManager.cancel(notificationId)

            // 3. यहाँ आप अपनी API कॉल कर सकते हैं कि कॉल रिजेक्ट हो गई है
            Log.d("CallReceiver", "Call Rejected")
        }

        if (action == "ACTION_ANSWER") {
            // यहाँ कॉल उठाने का लॉजिक लिखें (Activity खोलें)
//            val intentToActivity = Intent(context, VideoCallActivity::class.java)
//            intentToActivity.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//            context.startActivity(intentToActivity)

            notificationManager.cancel(notificationId)
        }
    }
}