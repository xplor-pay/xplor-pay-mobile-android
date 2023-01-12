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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MerchantSelectViewModel @Inject constructor(
    private val sharedPrefs: SharedPreferencesDataSource,
    private val remoteDataSource: RemoteDataSource
) : ViewModel() {

    private val _merchantFlow = MutableStateFlow<Merchant?>(null)
    val merchantFlow: Flow<Merchant?> = _merchantFlow

    private val _selectedTerminalFlow = MutableStateFlow<Terminal?>(null)
    val selectedTerminalFlow: Flow<Terminal?> = _selectedTerminalFlow

    private val _terminalsFlow = MutableStateFlow<List<Terminal>>(emptyList())
    val terminalsFlow: Flow<List<Terminal>> = _terminalsFlow

    private val _loadingFlow = MutableStateFlow(true)
    val loadingFlow: Flow<Boolean> = _loadingFlow

    fun fetchMerchantAndTerminal() {
        viewModelScope.launch {
            _loadingFlow.emit(true)
            sharedPrefs.getMerchant()?.also { merchant ->
                _merchantFlow.emit(merchant)
                sharedPrefs.getTerminal()?.also { terminal ->
                    _selectedTerminalFlow.emit(terminal)
                } ?: run {
                    _selectedTerminalFlow.emit(null)
                }
                withContext(Dispatchers.IO) {
                    fetchTerminals(merchant.merchantNumber)
                    _loadingFlow.emit(false)
                }
            } ?: run {
                _loadingFlow.emit(false)
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