package com.xplore.paymobile.ui.merchantselection.search.terminal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clearent.idtech.android.wrapper.ClearentCredentials
import com.clearent.idtech.android.wrapper.ClearentWrapper
import com.xplore.paymobile.data.datasource.SharedPreferencesDataSource
import com.xplore.paymobile.data.remote.model.Terminal
import com.xplore.paymobile.data.web.VtTokenRefreshManager
import com.xplore.paymobile.ui.merchantselection.search.list.MerchantsListAdapter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TerminalSearchViewModel @Inject constructor(
    private val sharedPrefs: SharedPreferencesDataSource,
    private val vtTokenRefreshManager: VtTokenRefreshManager,
) : ViewModel() {

    private val clearentWrapper = ClearentWrapper.getInstance()

    private val _sortedTerminalsFlow = MutableStateFlow<List<Terminal>>(listOf())
    val terminalsFlow: Flow<List<Terminal>> = _sortedTerminalsFlow

    private var terminals: List<Terminal> = emptyList()

    fun setTerminals(list: List<Terminal>) {
        terminals = list
        viewModelScope.launch {
            _sortedTerminalsFlow.emit(terminals)
        }
    }

    fun searchForQuery(query: String) {
        viewModelScope.launch {
            _sortedTerminalsFlow.emit(
                terminals.filter {
                    it.terminalName.contains(
                        query,
                        ignoreCase = true,
                    )
                },
            )
        }
    }

    fun saveTerminal(merchantItem: MerchantsListAdapter.MerchantItem) {
        val selectedTerminal = terminals.find { item ->
            item.terminalPKId == merchantItem.id
        }
        selectedTerminal?.also { terminal ->
            sharedPrefs.setTerminal(terminal)
            clearentWrapper.sdkCredentials.clearentCredentials =
                ClearentCredentials.MerchantHomeApiCredentials(
                    merchantId = merchantItem.id,
                    vtToken = terminal.questJwt.token,
                )
            vtTokenRefreshManager.startTimer(false)
        }
    }
}
