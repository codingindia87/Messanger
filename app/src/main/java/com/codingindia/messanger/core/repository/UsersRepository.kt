package com.codingindia.messanger.core.repository

import android.net.Uri
import android.util.Log
import com.codingindia.messanger.core.dao.UserDao
import com.codingindia.messanger.features.home.domain.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UsersRepository @Inject constructor(
    private val database: FirebaseDatabase,
    private val userDao: UserDao,
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage
) {
    private val currentUser = auth.currentUser?.uid!!

    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val statusRef = database.getReference("users")

    private val statusListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {

            val user: User = snapshot.getValue(User::class.java)!!
            user.id = snapshot.key!!

            Log.d("UsersRepository", user.toString())

            coroutineScope.launch {
                userDao.updateUser(user)
            }
        }

        override fun onCancelled(error: DatabaseError) {

        }
    }

    fun getAllUsersFromRoom(): Flow<List<User>> {
        return userDao.getAllUsers()
    }

    suspend fun refreshUsersFromFirestore() {
        try {
            val snapshot = database.getReference("users").get().await()
            val users = snapshot.children.mapNotNull { usersSnap ->
                usersSnap.getValue(User::class.java)?.copy(id = usersSnap.key!!)
            }
            val filterUser = users.filter { it.id != auth.uid }
            userDao.insertAll(filterUser)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("UsersRepository-Error", e.message.toString())
        }
    }

    fun getUserById(userId: String): Flow<User?> {
        return userDao.getUserById(userId)
    }

    fun startListeningForStatusUpdates(userId: String) {
        statusRef.child(userId).addValueEventListener(statusListener)
    }

    fun stopListeningForStatusUpdates() {
        statusRef.removeEventListener(statusListener)
    }

    fun updateChatRoom(roomId: String?) {
        statusRef.child(currentUser).child("chatRoom").setValue(roomId)
    }

    fun setTyping(isTyping: Boolean) {
        statusRef.child(currentUser).child("typing").setValue(isTyping)
    }

    suspend fun fetchUserProfile(): Result<User> {
        try {
            val ref = database.getReference("users").child(currentUser)
            val userSnap = ref.get().await()
            val user = userSnap.getValue(User::class.java)
            return Result.success(user!!)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    suspend fun updateUserProfile(uri: Uri): Result<Unit> {
        try {
            val storageRef = storage.getReference(currentUser).child("profile-image")
            storageRef.putFile(uri).await()
            val downloadLink = storageRef.downloadUrl.await()
            database.getReference("users").child(currentUser).child("imageUrl")
                .setValue(downloadLink.toString()).await()
            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    suspend fun updateUserName(newName: String): Result<Unit> {
        try {
            database.getReference("users").child(auth.uid!!).child("name").setValue(newName).await()
            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
}