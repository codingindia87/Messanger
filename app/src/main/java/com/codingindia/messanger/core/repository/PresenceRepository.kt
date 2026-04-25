package com.codingindia.messanger.core.repository

interface PresenceRepository {
    fun updateUserStatus(isOnline: Boolean)
    fun configureDisconnect()
}
