package com.xplore.paymobile.ui.merchantselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clearent.idtech.android.wrapper.ClearentCredentials
import com.clearent.idtech.android.wrapper.ClearentWrapper
import com.clearent.idtech.android.wrapper.http.model.TerminalSettingsResponse
import com.xplore.paymobile.data.datasource.NetworkResource
import com.xplore.paymobile.data.datasource.RemoteDataSource
import com.xplore.paymobile.data.datasource.SharedPreferencesDataSource
import com.xplore.paymobile.data.remote.model.MerchantTerminal
import com.xplore.paymobile.data.remote.model.Terminal
import com.xplore.paymobile.data.remote.model.TerminalsResponse
import com.xplore.paymobile.data.web.Merchant
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

// NOTE: once the app can call cgw with the edge token, the quest-jwt call will be unnecessary

@HiltViewModel
class MerchantSelectViewModel @Inject constructor(
    private val sharedPrefs: SharedPreferencesDataSource,
    private val remoteDataSource: RemoteDataSource,
) : ViewModel() {

    private val _merchantFlow = MutableStateFlow<Merchant?>(null)
    val merchantFlow: StateFlow<Merchant?> = _merchantFlow

    private val clearentWrapper = ClearentWrapper.getInstance()

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
            }
        }
    }

    private suspend fun fetchTerminals(merchantId: String) {
        // TODO look into this later.  could be a time saver if we are caching terminals.
//             multiple terminals could be an issue...
//        if (sharedPrefs.getTerminal() != null) {
//            setClearentCredentials(merchantId, sharedPrefs.getTerminal()!!)
//            return
//        }
        val networkResponse = remoteDataSource.fetchTerminals(merchantId)
        if (networkResponse is NetworkResource.Success && networkResponse.data != null) {
            val terminals = filterMobileTerminals(networkResponse.data, merchantId)
            if (terminals.isNotEmpty() && sharedPrefs.getTerminal() != null) {
                val currentTerminal = sharedPrefs.getTerminal()
                for (terminal in terminals) {
                    if (terminal.terminalPKId == currentTerminal?.terminalPKId) {
                        sharedPrefs.setTerminal(terminal)
                        setClearentCredentials(merchantId, terminal)
                        if (sharedPrefs.getTerminalTimezone().isNullOrBlank()) {
                            getTerminalSettings(merchantId)
                        }
                    }
                }
            } else if (terminals.isNotEmpty() && terminals.size == 1) {
                val selectedTerminal: Terminal = terminals[0]
                sharedPrefs.setTerminal(selectedTerminal)
                setClearentCredentials(merchantId, selectedTerminal)
                if (sharedPrefs.getTerminalTimezone().isNullOrBlank()) {
                    getTerminalSettings(merchantId)
                }
            }

            _terminalsFlow.emit(terminals)
        } else {
            _terminalsFlow.emit(emptyList())
        }
    }

    private fun setClearentCredentials(merchantId: String, selectedTerminal: Terminal) {
        clearentWrapper.sdkCredentials.clearentCredentials =
            ClearentCredentials.MerchantHomeApiCredentials(
                merchantId = merchantId,
                vtToken = selectedTerminal.questJwt.token,
            )
    }

    private suspend fun filterMobileTerminals(terminals: TerminalsResponse, merchantId: String): List<Terminal> {
        val mobileTerminals = getMobileTerminals(merchantId)
        if (mobileTerminals.isEmpty()) {
            Timber.d("No mobile terminals returned from the response for merchant $merchantId")
            return emptyList()
        }

        val filteredTerminals = terminals.filter { terminal ->
            mobileTerminals.any { mobileTerminal ->
                terminal.terminalPKId == mobileTerminal.merchantTerminalId
            }
        }

        return filteredTerminals
    }

    private suspend fun getMobileTerminals(merchantId: String): List<MerchantTerminal> {
        return when (val networkResponse = remoteDataSource.getMobileTerminals(merchantId)) {
            is NetworkResource.Success -> {
                val mobileTerminals = networkResponse.data?.merchantTerminalsPayload?.merchantTerminals.orEmpty()
                Timber.d("Found ${mobileTerminals.size} mobile terminals for merchant $merchantId")
                mobileTerminals
            }
            else -> emptyList()
        }
    }

    suspend fun getTerminalSettings(merchantId: String) {
        val networkResponse = remoteDataSource.getTerminalSettings(merchantId)
        if (networkResponse is NetworkResource.Success) {
            val terminalSettings = networkResponse.data as TerminalSettingsResponse
            terminalSettings.payload.terminalSettings.let {
                sharedPrefs.setTerminalSettings(
                    it,
                )
            }
        }
    }

    fun getMerchant() = sharedPrefs.getMerchant()
    fun getTerminal() = sharedPrefs.getTerminal()

    fun isLoggedIn() = sharedPrefs.isLoggedIn()
}

sealed class TerminalSelection {
    data class TerminalAvailable(val terminal: Terminal) : TerminalSelection()
    object NoTerminal : TerminalSelection()
}
