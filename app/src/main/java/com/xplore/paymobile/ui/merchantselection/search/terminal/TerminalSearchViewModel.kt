package com.xplore.paymobile.ui.merchantselection.search.terminal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xplore.paymobile.data.datasource.SharedPreferencesDataSource
import com.xplore.paymobile.data.remote.model.Terminal
import com.xplore.paymobile.ui.merchantselection.search.list.MerchantsListAdapter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TerminalSearchViewModel @Inject constructor(
    private val sharedPrefs: SharedPreferencesDataSource
) :
    ViewModel() {

    private val _sortedTerminalsFlow = MutableStateFlow<List<Terminal>>(listOf())
    val terminalsFlow: Flow<List<Terminal>> = _sortedTerminalsFlow

    private var terminals: List<Terminal> = emptyList()

    fun setTerminals(list: List<Terminal>) {
        terminals = list
        viewModelScope.launch {
            Timber.d("TESTEST emit ${terminals.size}")
            _sortedTerminalsFlow.emit(terminals)
        }
    }

    fun searchForQuery(query: String) {
        viewModelScope.launch {
            _sortedTerminalsFlow.emit(terminals.filter {
                it.terminalName.contains(
                    query,
                    ignoreCase = true
                )
            })
        }
    }

    fun saveTerminal(terminal: MerchantsListAdapter.MerchantItem) {
        val toSave = terminals.find { item ->
            item.terminalPKId == terminal.id
        }
        toSave?.let {
            sharedPrefs.setTerminal(toSave)
        }
    }
}