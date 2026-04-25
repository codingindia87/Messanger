package com.codingindia.messanger.core.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.codingindia.messanger.core.dao.MessageDao
import com.codingindia.messanger.core.utils.Conts
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

@HiltWorker
class UploadWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    @Assisted private val auth: FirebaseAuth,
    @Assisted private val firebaseStorage: FirebaseStorage,
    @Assisted private val messageDao: MessageDao
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val KEY_URIS = "image_uri"
        const val KEY_DOWNLOAD_URL = "download_url"
        const val KEY_PATH = "path"
        const val MESSAGE_ID = "message_id"

        // Notification constants
        private const val NOTIFICATION_ID = 101
        private const val CHANNEL_ID = "upload_channel"
        private const val CHANNEL_NAME = "Media Uploads"
    }

    private val notificationManager =
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


    override suspend fun doWork(): Result {
        val uriStrings = inputData.getStringArray(KEY_URIS) ?: return Result.failure()
        val basePath = inputData.getString(KEY_PATH) ?: "send"
        val messageID = inputData.getLong(MESSAGE_ID, 0L)

        val workerScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        // 1. Initialize Notification Channel (Required for Android O+)
        createNotificationChannel()

        // 2. Start Foreground immediately to prevent process death
        setForeground(createForegroundInfo("Preparing uploads...", 0, 0, true))

        messageDao.updateIsUploading(messageID, true)
        return try {
            val downloadList: MutableList<String> = mutableListOf()
            val totalFiles = uriStrings.size

            for ((index, uriString) in uriStrings.withIndex()) {
                val originalUri = uriString.toUri()

                // Update notification
                setForeground(
                    createForegroundInfo(
                        "Uploading ${index + 1} of $totalFiles", index, totalFiles, false
                    )
                )

                // 3. CRITICAL FIX: Copy file to internal cache
                // This prevents "No persistable permission grants found" errors
                val localFile = copyUriToCache(appContext, originalUri)
                if (localFile == null) {
                    Log.e("UploadWorker", "Failed to copy file to cache: $uriString")
                    continue // Skip this file if we can't read it
                }

                // Use the local file URI for all subsequent operations
                val localUri = Uri.fromFile(localFile)
                val mimeType = getMimeType(originalUri) ?: "application/octet-stream"
                val extension = getExtension(originalUri, mimeType)

                mimeType.startsWith("video/")
                val isImage = mimeType.startsWith("image/")

                val fileName = "${UUID.randomUUID()}.$extension"
                val storageRef =
                    firebaseStorage.getReference(auth.uid!!).child("$basePath/$fileName")

                val metadata = StorageMetadata.Builder().setContentType(mimeType).build()

                // 4. Process & Upload
                val uploadTask = if (isImage) {
                    val encryptedData = encryptAndCompressImage(localUri)
                    if (encryptedData != null) storageRef.putBytes(encryptedData, metadata)
                    else storageRef.putFile(localUri, metadata)
                } else {
                    storageRef.putFile(localUri, metadata)
                }

                uploadTask.addOnProgressListener { taskSnapshot ->
                    val percent =
                        (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()

                    // डेटाबेस अपडेट करें
                    workerScope.launch {
                        messageDao.updateProgress(messageID, percent)
                    }

                    // नोटिफिकेशन अपडेट करें
                    setForegroundAsync(
                        createForegroundInfo(
                            "Uploading $percent%", index + 1, totalFiles, false
                        )
                    )
                }

                uploadTask.await()

                val downloadUrl = storageRef.downloadUrl.await().toString()
                downloadList.add(downloadUrl)

                // 5. CLEANUP: Delete the temp file to save space
                try {
                    if (localFile.exists()) {
                        localFile.delete()
                    }
                } catch (e: Exception) {
                    Log.w("UploadWorker", "Failed to delete temp file: ${localFile.absolutePath}")
                }
            }

            messageDao.updateProgress(messageID, 100)
            val outputData = workDataOf(KEY_DOWNLOAD_URL to downloadList.toTypedArray())
            Result.success(outputData)


        } catch (e: Exception) {
            Log.e("UploadWorker", "Upload Error: ${e.message}")
            Result.retry()

        }
    }

    private fun createForegroundInfo(
        progress: String, current: Int, total: Int, indeterminate: Boolean
    ): ForegroundInfo {
        val notification =
            NotificationCompat.Builder(appContext, CHANNEL_ID).setContentTitle("Sending Media")
                .setContentText(progress)
                .setSmallIcon(android.R.drawable.stat_sys_upload) // REPLACE with R.drawable.ic_upload
                .setOngoing(true).setProgress(total, current, indeterminate)
                .setPriority(NotificationCompat.PRIORITY_LOW).build()

        // Android 14 (API 34) requires declaring the service type
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows progress for media uploads"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun copyUriToCache(context: Context, uri: Uri): File? {
        return try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri) ?: return null

            // Create a temp file in the cache directory
            val tempFile = File(
                context.cacheDir, "upload_temp_${System.currentTimeMillis()}_${UUID.randomUUID()}"
            )
            val outputStream = FileOutputStream(tempFile)

            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            tempFile
        } catch (e: Exception) {
            Log.e("UploadWorker", "Error copying file to cache: ${e.message}")
            null
        }
    }

    private fun getMimeType(uri: Uri): String? {
        // Since we are now sometimes checking 'file://' URIs (our cache), we need to handle that
        if (uri.scheme == "content") {
            return appContext.contentResolver.getType(uri)
        } else {
            val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase())
        }
    }

    private fun getExtension(uri: Uri, mimeType: String): String {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "file"
    }

    private fun compressImage(uri: Uri): ByteArray? {
        return try {
            // contentResolver.openInputStream works for both content:// and file://
            val inputStream = appContext.contentResolver.openInputStream(uri)
            var bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap == null) return null

            bitmap = rotateImageIfRequired(uri, bitmap)

            val outputStream = ByteArrayOutputStream()
            // Compress JPEG to 70% quality
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)

            bitmap.recycle()
            outputStream.toByteArray()
        } catch (e: Exception) {
            Log.e("UploadWorker", "Image Compression error: ${e.message}")
            null
        }
    }

    private fun rotateImageIfRequired(uri: Uri, bitmap: Bitmap): Bitmap {
        try {
            val inputStream = appContext.contentResolver.openInputStream(uri) ?: return bitmap
            val exif = ExifInterface(inputStream)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
            )
            inputStream.close()

            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                else -> return bitmap
            }
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: Exception) {
            return bitmap
        }
    }

    private fun encryptAndCompressImage(uri: Uri): ByteArray? {
        return try {
            // 1. Image Load करना
            val inputStream = appContext.contentResolver.openInputStream(uri)
            var bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            if (bitmap == null) return null
            bitmap = rotateImageIfRequired(uri, bitmap)

            // 2. Compress करना
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val plainBytes = outputStream.toByteArray()
            bitmap.recycle()

            // 3. Static AES Encryption
            val keySpec = SecretKeySpec(Conts.SECRET_KEY.toByteArray(), "AES")
            val ivSpec = IvParameterSpec(Conts.IV.toByteArray())

            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding") // यही मोड रखें
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)

            cipher.doFinal(plainBytes) // ये बाइट्स Firebase पर अपलोड करें
        } catch (e: Exception) {
            Log.e("UploadWorker", "Encryption error: ${e.message}")
            null
        }
    }
}
