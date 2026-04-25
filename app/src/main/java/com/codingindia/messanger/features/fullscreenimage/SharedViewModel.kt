package com.codingindia.messanger.features.fullscreenimage

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// In your SharedViewModel.kt
class SharedViewModel : ViewModel() {

    private val _imageUris = MutableStateFlow<List<String>>(emptyList())
    val imageUris = _imageUris.asStateFlow()

    fun setImagesForSlider(uris: List<String>) {
        _imageUris.value = uris
    }
}
