package com.xplore.paymobile.ui.merchantselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xplore.paymobile.data.remote.model.Terminal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MerchantSelectSharedViewModel : ViewModel() {

    var terminals: List<Terminal> = emptyList()

    private val _allowNext = MutableStateFlow(true)
    val allowNext: Flow<Boolean> = _allowNext

    fun setAllowNext(allow: Boolean) {
        viewModelScope.launch {
            _allowNext.emit(allow)
        }
    }
}