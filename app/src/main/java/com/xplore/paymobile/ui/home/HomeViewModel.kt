package com.xplore.paymobile.ui.home

import androidx.lifecycle.ViewModel
import com.clearent.idtech.android.wrapper.SDKWrapper
import com.clearent.idtech.android.wrapper.model.ReaderState
import com.clearent.idtech.android.wrapper.model.ReaderStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel : ViewModel() {

    companion object {
        private val defaultReaderState = ReaderState.NoReader
    }

    private val _readerState = MutableStateFlow<ReaderState>(defaultReaderState)
    val readerState: StateFlow<ReaderState> = _readerState

    fun getCurrentReader(): ReaderStatus? = SDKWrapper.currentReader
}