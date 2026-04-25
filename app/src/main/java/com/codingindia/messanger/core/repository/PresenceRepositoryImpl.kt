package com.codingindia.messanger.core.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import javax.inject.Inject

class PresenceRepositoryImpl @Inject constructor(
    private val database: FirebaseDatabase,
    private val auth: FirebaseAuth
) : PresenceRepository {

    private val currentUser = auth.currentUser

    override fun updateUserStatus(isOnline: Boolean) {
        if (currentUser != null) {
            val userStatusRef = database.getReference("users")
                .child(currentUser.uid)
            val status = mapOf(
                "online" to isOnline,
                "lastSeen" to ServerValue.TIMESTAMP
            )
            userStatusRef.updateChildren(status)
        }
    }

    override fun configureDisconnect() {
        if (currentUser != null) {
            val userStatusRef = database.getReference("users")
                .child(currentUser.uid)
            val offlineStatus = mapOf(
                "online" to false,
                "lastSeen" to ServerValue.TIMESTAMP
            )
            userStatusRef.onDisconnect().updateChildren(offlineStatus)
        }
    }
}
