package com.xplore.paymobile.ui.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.clearent.idtech.android.wrapper.SDKWrapper
import com.clearent.idtech.android.wrapper.model.ReaderState
import com.clearent.idtech.android.wrapper.model.ReaderStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    companion object {
        private const val firstPairDoneKey = "FIRST_PAIR_DONE_KEY"
    }

    fun firstPairDone() {
        savedStateHandle[firstPairDoneKey] = true
    }

    fun isFirstPairDone() = savedStateHandle[firstPairDoneKey] ?: false
}