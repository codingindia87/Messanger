package com.codingindia.messanger.features.updates

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codingindia.messanger.core.repository.AppUpdateRepository
import com.codingindia.messanger.features.updates.domain.UpdateUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class UpdateViewmodel @Inject constructor(
    private val appUpdateRepository: AppUpdateRepository
): ViewModel() {

    private val _uiState = MutableStateFlow<UpdateUiState>(UpdateUiState.Idle)
    val uiState: StateFlow<UpdateUiState> = _uiState.asStateFlow()

    fun checkForUpdates(context: Context) {
        viewModelScope.launch {
            _uiState.value = UpdateUiState.Loading

            try {
                val remoteUpdate = appUpdateRepository.fetchLastUpdate()
                val currentVersionCode = getCurrentVersionCode(context)

                if (remoteUpdate != null) {
                    val remoteCode = remoteUpdate.version ?: 0

                    if (remoteCode > currentVersionCode) {
                        // Check if file exists
                        val apkFile = appUpdateRepository.getDownloadFile(remoteCode.toString())
                        val isDownloaded = apkFile.exists()

                        _uiState.value = UpdateUiState.Success(
                            update = remoteUpdate,
                            isDownloaded = isDownloaded,
                            apkFile = apkFile
                        )
                    } else {
                        _uiState.value = UpdateUiState.Error("App is up to date")
                    }
                } else {
                    _uiState.value = UpdateUiState.Error("No update information found")
                }
            } catch (e: Exception) {
                _uiState.value = UpdateUiState.Error(e.message ?: "Failed to check")
            }
        }
    }

    fun downloadUpdate(url: String, version: String) {
        viewModelScope.launch {
            appUpdateRepository.downloadApk(url, version)
            // Ideally, you should listen to download progress here to update UI
            // For now, we assume download starts and user waits for notification
        }
    }

    // Function to trigger installation Intent
    fun installApk(context: Context, file: File) {
        // 1. Check if "Install Unknown Apps" permission is required (Android 8.0+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                // Permission is missing. Open Settings for the user to allow it.
                try {
                    val permissionIntent = Intent(
                        android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES
                    ).apply {
                        data = Uri.parse("package:${context.packageName}")
                        // FLAG_ACTIVITY_NEW_TASK is required when starting from a non-Activity context
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(permissionIntent)

                    // Log or notify that we are waiting for permission
                    Log.d("InstallFlow", "Redirecting user to enable 'Install Unknown Apps' permission")
                    return // Stop execution here. User must click 'Install' again after enabling.
                } catch (e: Exception) {
                    Log.e("InstallFlow", "Failed to open settings", e)
                    // If we can't open settings, we might as well try the install intent
                    // and let the system handle the error dialog.
                }
            }
        }

        // 2. Permission is granted (or not needed on < Android 8). Proceed with Install.
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider", // Ensure this matches AndroidManifest.xml
                file
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(installIntent)
        } catch (e: Exception) {
            _uiState.value = UpdateUiState.Error("Install failed: ${e.message}")
            Log.e("Install Error", "Install failed: ${e.message}")
        }
    }


    private fun getCurrentVersionCode(context: Context): Int {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                pInfo.versionCode
            }
        } catch (e: Exception) {
            -1
        }
    }
}
