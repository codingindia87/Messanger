package com.codingindia.messanger.features.home.updates

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.codingindia.messanger.core.repository.UpdateRepository
import com.codingindia.messanger.features.home.domain.User
import com.codingindia.messanger.features.home.updates.domain.Update
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UpdatesViewModel @Inject constructor(
    private val updateRepository: UpdateRepository,
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val currentUserId: String? = auth.currentUser?.uid

    // --- Status List State ---
    private val _allStatuses = MutableStateFlow<List<Update>>(emptyList())
    val allStatuses: StateFlow<List<Update>> = _allStatuses.asStateFlow()

    // --- Loading States ---
    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private val _isFetchingUpdates = MutableStateFlow(true)
    val isFetchingUpdates: StateFlow<Boolean> = _isFetchingUpdates.asStateFlow()

    // --- NEW: Views List State ---
    private val _viewersList = MutableStateFlow<List<User>>(emptyList())
    val viewersList: StateFlow<List<User>> = _viewersList.asStateFlow()

    // Keep track of the current view fetching job to cancel it if we switch statuses
    private var viewFetchingJob: Job? = null

    init {
        getAllUpdates()
    }

    private fun getAllUpdates() {
        viewModelScope.launch {
            _isFetchingUpdates.value = true
            updateRepository.getAllStatus().catch { e ->
                    _allStatuses.value = emptyList()
                    _isFetchingUpdates.value = false
                }.collect { list ->
                    _allStatuses.value = list
                    _isFetchingUpdates.value = false
                }
        }
    }

    fun sendStatus(uris: List<Uri>) {
        viewModelScope.launch {
            try {
                val workId = updateRepository.updateStatus(uris)
                _isUploading.value = true
                WorkManager.getInstance(context).getWorkInfoByIdFlow(workId).collect { workInfo ->
                        if (workInfo != null) {
                            when (workInfo.state) {
                                WorkInfo.State.SUCCEEDED -> {
                                    _isUploading.value = false
                                }

                                WorkInfo.State.FAILED, WorkInfo.State.CANCELLED -> {
                                    _isUploading.value = false
                                }

                                else -> {
                                    _isUploading.value = true
                                }
                            }
                        }
                    }
            } catch (e: Exception) {
                _isUploading.value = false
                e.printStackTrace()
            }
        }
    }

    fun deleteStatus(id: String, deleteUri: String, updateList: List<String>) {
        updateRepository.deleteStatus(id, deleteUri, updateList)
    }

    fun updateViews(statusId: String) {
        updateRepository.updateViews(statusId)
    }

    // --- NEW FUNCTION: Call this when opening the "Views" BottomSheet ---
    fun getViews(statusId: String) {
        // 1. Cancel previous job if user switches between statuses quickly
        viewFetchingJob?.cancel()

        // 2. Start new observation
        viewFetchingJob = viewModelScope.launch {
            // Optional: Reset list to empty while loading
            _viewersList.value = emptyList()

            updateRepository.getAllViews(statusId).catch { e ->
                    e.printStackTrace()
                    _viewersList.value = emptyList()
                }.collect { users ->
                    _viewersList.value = users
                }
        }
    }
}
