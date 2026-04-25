package com.codingindia.messanger.core.notification.receiver

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceManager @Inject constructor(@ApplicationContext context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("MessahgerPrefs", Context.MODE_PRIVATE)

    companion object {
        const val KEY_ACTIVE_CHAT_UID = "active_chat_uid"
    }

    private fun key(senderId: String) = "unread_$senderId"

    fun setActiveChatUserId(uid: String?) {
        with(prefs.edit()) {
            if (uid == null) {
                remove(KEY_ACTIVE_CHAT_UID)
            } else {
                putString(KEY_ACTIVE_CHAT_UID, uid)
            }
            apply()
        }
    }

    fun getActiveChatUserId(): String? {
        return prefs.getString(KEY_ACTIVE_CHAT_UID, null)
    }

    fun getUnread(senderId: String): Int = prefs.getInt(key(senderId), 0)

    fun incrementUnread(senderId: String): Int {
        val next = getUnread(senderId) + 1
        prefs.edit().putInt(key(senderId), next).apply()
        return next
    }

    fun resetUnread(senderId: String) {
        prefs.edit().putInt(key(senderId), 0).apply()
    }

    fun getTotalUnread(senderIds: List<String>): Int {
        return senderIds.sumOf { getUnread(it) }
    }
}
