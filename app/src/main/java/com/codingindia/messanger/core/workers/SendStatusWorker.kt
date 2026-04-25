package com.codingindia.messanger.core.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.codingindia.messanger.features.home.updates.domain.Update
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await

@HiltWorker
class SendStatusWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    @Assisted private val database: FirebaseDatabase
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val KEY_STATUS = "status"
    }

    override suspend fun doWork(): Result {
        val statusJson = inputData.getString(KEY_STATUS)
        val newUris = inputData.getStringArray(UploadWorker.KEY_DOWNLOAD_URL)

        Log.d("SendStatusWorker", "Status: $statusJson")
        Log.d("SendStatusWorker", "New Uris: ${newUris?.toList()}")

        if (statusJson == null || newUris == null) {
            return Result.failure()
        }

        // Deserialize the input status (contains user info mostly)
        val inputStatus = Gson().fromJson(statusJson, Update::class.java)
        val userId = inputStatus.uid ?: return Result.failure()

        return try {
            val ref = database.getReference("status")

            // 1. Check if a status entry already exists for this user
            // We query where the child "userId" matches our current userId
            val query = ref.orderByChild("uid").equalTo(userId)

            val snapshot = query.get().await()

            if (snapshot.exists()) {
                // --- CASE 1: UPDATE EXISTING STATUS ---
                // Since userId is unique per user status block (assumption), we get the first child
                val existingChild = snapshot.children.first()
                val existingKey = existingChild.key

                // Get current list of URLs
                val existingStatus = existingChild.getValue(Update::class.java)
                val currentUrls = existingStatus?.url?.toMutableList() ?: mutableListOf()

                // Append new URLs
                currentUrls.addAll(newUris)

                // Update the URL list and timestamp
                val updates = mapOf(
                    "url" to currentUrls,
                    "timeAgo" to System.currentTimeMillis() // Update timestamp to now
                )

                if (existingKey != null) {
                    ref.child(existingKey).updateChildren(updates).await()
                }

            } else {
                // --- CASE 2: CREATE NEW STATUS ---
                val key = ref.push().key ?: return Result.failure()

                // Set the URLs for the new status
                inputStatus.url = newUris.toList()
                inputStatus.timeAgo = System.currentTimeMillis()

                ref.child(key).setValue(inputStatus).await()
            }

            Result.success()

        } catch (e: Exception) {
            Log.e("SendStatusWorker", "Error uploading data: ${e.message}")
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
