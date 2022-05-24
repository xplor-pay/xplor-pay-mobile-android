package com.xplore.paymobile.ui.home

import androidx.lifecycle.ViewModel
import com.xplore.paymobile.model.ReaderState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel : ViewModel() {

    companion object {
        private val defaultReaderState = ReaderState.NoReader
    }

    private val _readerState = MutableStateFlow<ReaderState>(defaultReaderState)
    val readerState: StateFlow<ReaderState> = _readerState
}