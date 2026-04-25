package com.codingindia.messanger.core.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.codingindia.messanger.core.workers.SendStatusWorker
import com.codingindia.messanger.core.workers.UploadWorker
import com.codingindia.messanger.features.home.domain.User
import com.codingindia.messanger.features.home.updates.domain.Update
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UpdateRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase,
    private val storage: FirebaseStorage,
) {
    val currentUser = auth.currentUser?.uid!!

    fun updateStatus(uris: List<Uri>): java.util.UUID {
        val constraints =
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

        val stringUris = uris.map { it.toString() }

        val uploadWorkRequests = OneTimeWorkRequestBuilder<UploadWorker>().setInputData(
            workDataOf(
                UploadWorker.KEY_URIS to stringUris.toTypedArray(),
                UploadWorker.KEY_PATH to "status"
            )
        ).setConstraints(constraints).build()

        val status = Update(
            uid = currentUser, timeAgo = System.currentTimeMillis(), captions = "Status"

        )
        val sendStatusWorker = OneTimeWorkRequestBuilder<SendStatusWorker>().setInputData(
            workDataOf(
                SendStatusWorker.KEY_STATUS to Gson().toJson(status)
            )
        ).setConstraints(constraints).build()


        WorkManager.getInstance(context)
            .beginWith(uploadWorkRequests) // Start with the parallel uploads
            .then(sendStatusWorker)   // Run this after all uploads succeed
            .enqueue()

        return sendStatusWorker.id
    }

    fun getAllStatus(): Flow<List<Update>> = callbackFlow {
        val statusRef = database.getReference("status")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                launch { // Coroutine scope to handle sequential/parallel user data fetching
                    mutableListOf<Update>()

                    // 1. सभी स्टेटस को मैप करें
                    val statusList = snapshot.children.mapNotNull { child ->
                        // Get the object and immediately assign the key to the 'id' field
                        child.getValue(Update::class.java)?.apply {
                            id = child.key ?: ""
                        }
                    }
                    // 2. हर स्टेटस के लिए यूजर डेटा फेच करें
                    val updatedList = statusList.map { update ->
                        val userSnapshot =
                            database.getReference("users/${update.uid}").get().await()

                        // यूजर का नाम और इमेज निकालें (मान लीजिए आपके User मॉडल में ये फील्ड्स हैं)
                        val name = userSnapshot.child("name").getValue(String::class.java)
                        val image = userSnapshot.child("imageUrl").getValue(String::class.java)

                        // Update मॉडल में डेटा भरें
                        update.copy(
                            userName = name ?: "Unknown User", userImage = image
                        )
                    }

                    Log.d("UpdateRepository", updatedList.toString())


                    trySend(updatedList)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
                Log.d("UpdateRepository", error.message)
            }
        }

        statusRef.addValueEventListener(listener)
        awaitClose { statusRef.removeEventListener(listener) }
    }

    fun deleteStatus(id: String, deleteUri: String, updateList: List<String>) {
        val storageRef = storage.getReferenceFromUrl(deleteUri)
        storageRef.delete().addOnCompleteListener {
            if (updateList.isEmpty()) {
                database.getReference("status").child(id).removeValue()
            } else {
                database.getReference("status").child(id).updateChildren(mapOf("url" to updateList))
            }
        }
    }

    fun updateViews(statusId: String) {
        val viewsRef = database.getReference("status").child(statusId).child("views")

        viewsRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                // 1. Get the current list.
                // Using GenericTypeIndicator ensures we read the data correctly as a List<String>
                val t = object : GenericTypeIndicator<MutableList<String>>() {}
                var viewsList = currentData.getValue(t)

                // 2. "list is not found create a list"
                if (viewsList == null) {
                    viewsList = mutableListOf()
                }

                // 3. "check curent user is exit or not"
                if (!viewsList.contains(currentUser)) {
                    // 4. "else add new curentnt uid in this list"
                    viewsList.add(currentUser)

                    // Update the MutableData with the new list
                    currentData.value = viewsList
                }

                // 5. "if exit so not uodate list"
                // (If we didn't change currentData.value inside the if-block, the DB remains untouched)
                return Transaction.success(currentData)
            }

            override fun onComplete(
                error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?
            ) {
                if (error != null) {
                    Log.e("UpdateRepository", "Failed to update views: ${error.message}")
                } else {
                    // Log.d("UpdateRepository", "View updated successfully: $committed")
                }
            }
        })
    }

    fun getAllViews(statusId: String): Flow<List<User>> = callbackFlow {
        // 1. Reference the 'views' list for this status
        val viewsRef = database.getReference("status").child(statusId).child("views")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                launch {
                    // 2. Map snapshot to List<String> (UIDs)
                    val t = object : GenericTypeIndicator<List<String>>() {}
                    val uidList = snapshot.getValue(t) ?: emptyList()

                    if (uidList.isEmpty()) {
                        trySend(emptyList()) // No views yet
                    } else {
                        // 3. Fetch details for each UID in parallel
                        // Using map + await to gather all User objects
                        val viewersList = uidList.mapNotNull { viewerUid ->
                            try {
                                val userSnapshot =
                                    database.getReference("users").child(viewerUid).get().await()

                                // Manually extract fields or use getValue(User::class.java)
                                val name = userSnapshot.child("name").getValue(String::class.java)
                                    ?: "Unknown"
                                val image =
                                    userSnapshot.child("imageUrl").getValue(String::class.java)
                                        ?: ""

                                // Return a User object (Adjust Constructor as per your User model)
                                User(
                                    id = viewerUid, name = name, imageUrl = image
                                )
                            } catch (e: Exception) {
                                null // Skip if user fetch fails
                            }
                        }
                        trySend(viewersList)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        viewsRef.addValueEventListener(listener)
        awaitClose { viewsRef.removeEventListener(listener) }
    }


}