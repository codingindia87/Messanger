package com.codingindia.messanger.core.repository

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.codingindia.messanger.features.updates.domain.AppUpdate
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject

class AppUpdateRepository @Inject constructor(
    private val database: FirebaseDatabase,
    @ApplicationContext private val context: Context
) {

    suspend fun fetchLastUpdate(): AppUpdate? {
        return try {
            val snapshot = database.getReference("update").get().await()
            snapshot.getValue(AppUpdate::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Helper to get the File object for a specific version
    fun getDownloadFile(versionName: String): File {
        val fileName = "Messenger_Update_${versionName.replace(".", "_")}.apk"
        return File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
    }

    fun downloadApk(downloadUrl: String, versionName: String): Long {
        Log.d("DownloadAPk", "Starting download for: $downloadUrl")
        return try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val uri = Uri.parse(downloadUrl)
            val file = getDownloadFile(versionName)

            // Optional: Delete existing file if it's incomplete or old
            if (file.exists()) {
                file.delete()
            }

            val request = DownloadManager.Request(uri).apply {
                setTitle("Downloading Update $versionName")
                setDescription("Downloading application update...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, file.name)
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
            }
            downloadManager.enqueue(request)
        } catch (e: Exception) {
            Log.e("DownloadError", "Failed to start download: ${e.message}", e)
            -1L
        }
    }
}
