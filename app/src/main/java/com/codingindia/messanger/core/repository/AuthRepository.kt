package com.codingindia.messanger.core.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase,
    private val firebaseMessaging: FirebaseMessaging
) {

    suspend fun signIn(email: String, password: String): Result<Unit> {
        return try {
            val user = auth.signInWithEmailAndPassword(email, password).await()
            val token = firebaseMessaging.token.await()
            database.getReference("users").child(user.user?.uid!!).child("token").setValue(token)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}