package com.codingindia.messanger.features.updates.domain

import java.io.File

sealed interface UpdateUiState {
    object Idle : UpdateUiState
    object Loading : UpdateUiState
    data class Error(val message: String) : UpdateUiState

    // Updated Success state to include download status and file reference
    data class Success(
        val update: AppUpdate,
        val isDownloaded: Boolean = false,
        val apkFile: File? = null
    ) : UpdateUiState
}
