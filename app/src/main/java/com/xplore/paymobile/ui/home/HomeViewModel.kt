package com.xplore.paymobile.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xplore.paymobile.model.BatteryLifeState
import com.xplore.paymobile.model.Reader
import com.xplore.paymobile.model.ReaderState
import com.xplore.paymobile.model.SignalState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private var readerIndex = 0

    private val readers = listOf(
        ReaderState.ReaderIdle(Reader("1", "My reader")),
        ReaderState.ReaderPaired(
            Reader("1", "My reader"),
            SignalState.Weak,
            BatteryLifeState(97)
        ),
        ReaderState.ReaderPaired(
            Reader("2", "Mircea's reader"),
            SignalState.Weak,
            BatteryLifeState(33)
        ),
        ReaderState.ReaderPaired(
            Reader("3", "Macara's reader"),
            SignalState.Good,
            BatteryLifeState(18)
        ),
        ReaderState.ReaderPaired(
            Reader("4", "Marcel's reader"),
            SignalState.Weak,
            BatteryLifeState(55)
        ),
        ReaderState.ReaderPaired(
            Reader("4", "Marcel's reader"),
            SignalState.Medium,
            BatteryLifeState(47)
        ),
        ReaderState.NoReader,
    )

    companion object {
        private val defaultReaderState = ReaderState.NoReader
    }

    private val _readerState = MutableStateFlow<ReaderState>(defaultReaderState)
    val readerState: StateFlow<ReaderState> = _readerState

    fun cycleReaders() = viewModelScope.launch {
        readerIndex %= readers.size
        _readerState.emit(readers[readerIndex])
        readerIndex++
    }
}