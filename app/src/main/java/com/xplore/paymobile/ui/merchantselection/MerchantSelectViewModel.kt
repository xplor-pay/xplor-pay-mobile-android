package com.xplore.paymobile.ui.merchantselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xplore.paymobile.data.datasource.NetworkResource
import com.xplore.paymobile.data.datasource.RemoteDataSource
import com.xplore.paymobile.data.datasource.SharedPreferencesDataSource
import com.xplore.paymobile.data.remote.model.Terminal
import com.xplore.paymobile.data.remote.model.TerminalsResponse
import com.xplore.paymobile.data.web.Merchant
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MerchantSelectViewModel @Inject constructor(
    private val sharedPrefs: SharedPreferencesDataSource,
    private val remoteDataSource: RemoteDataSource
) : ViewModel() {

    private val _merchantFlow = MutableStateFlow<Merchant?>(null)
    val merchantFlow: StateFlow<Merchant?> = _merchantFlow

    private val _selectedTerminalFlow =
        MutableStateFlow<TerminalSelection>(TerminalSelection.NoTerminal)
    val selectedTerminalFlow: StateFlow<TerminalSelection> = sharedPrefs.terminalFlow.map {
        it?.let { terminal ->
            TerminalSelection.TerminalAvailable(terminal)
        } ?: TerminalSelection.NoTerminal
    }.stateIn(viewModelScope, SharingStarted.Eagerly, TerminalSelection.NoTerminal)

    private val _terminalsFlow = MutableStateFlow<List<Terminal>>(emptyList())
    val terminalsFlow: StateFlow<List<Terminal>> = _terminalsFlow

    private val _loadingFlow = MutableStateFlow(false)
    val loadingFlow: StateFlow<Boolean> = _loadingFlow

    init {
        viewModelScope.launch {
            sharedPrefs.merchantFlow.collectLatest { merchant ->
                _loadingFlow.emit(true)
                merchant?.also {
                    _merchantFlow.emit(it)
                    withContext(Dispatchers.IO) {
                        fetchTerminals(it.merchantNumber)
                        _loadingFlow.emit(false)
                    }
                } ?: run {
                    _loadingFlow.emit(false)
                }

                _selectedTerminalFlow.emit(
                    sharedPrefs.getTerminal()?.let { terminal ->
                        TerminalSelection.TerminalAvailable(terminal)
                    } ?: TerminalSelection.NoTerminal
                )
            }
        }
    }

    private suspend fun fetchTerminals(merchantId: String) {
        val networkResponse = remoteDataSource.fetchTerminals(merchantId)
        if (networkResponse is NetworkResource.Success) {
            val terminals = networkResponse.data as TerminalsResponse
            _terminalsFlow.emit(terminals)
        } else {
            _terminalsFlow.emit(emptyList())
        }
    }

    fun getMerchant() = sharedPrefs.getMerchant()
    fun getTerminal() = sharedPrefs.getTerminal()
}

sealed class TerminalSelection {
    data class TerminalAvailable(val terminal: Terminal) : TerminalSelection()
    object NoTerminal : TerminalSelection()
}