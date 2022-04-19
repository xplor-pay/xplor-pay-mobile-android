package com.xplore.paymobile.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xplore.paymobile.module.BatteryLifeState
import com.xplore.paymobile.module.Reader
import com.xplore.paymobile.module.ReaderState
import com.xplore.paymobile.module.SignalState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel () {

    private var readerIndex = 0

    private val readers = listOf(
        ReaderState.NoReader,
        ReaderState.ReaderIdle(Reader("1", "My reader")),
        ReaderState.ReaderPaired(
            Reader("1", "My reader"),
            SignalState.Weak,
            BatteryLifeState(97)
        ),
        ReaderState.ReaderPaired(
            Reader("2", "Mircea's reader"),
            SignalState.Weak,
            BatteryLifeState(93)
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
    )

    private val _readerState = MutableStateFlow(readers[readerIndex])
    val readerState: StateFlow<ReaderState> = _readerState

    fun cycleReaders() = viewModelScope.launch {
        readerIndex++
        readerIndex %= readers.size
        _readerState.emit(readers[readerIndex])
    }
}