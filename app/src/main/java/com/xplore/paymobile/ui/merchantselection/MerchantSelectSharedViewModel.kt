package com.xplore.paymobile.ui.merchantselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xplore.paymobile.data.datasource.SharedPreferencesDataSource
import com.xplore.paymobile.data.remote.model.Terminal
import com.xplore.paymobile.data.web.VTRefreshManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class MerchantSelectSharedViewModel : ViewModel() {

    var terminals: List<Terminal> = emptyList()

    private val _allowNext = MutableStateFlow(true)
    val allowNext: Flow<Boolean> = _allowNext

    fun setAllowNext(allow: Boolean) {
        viewModelScope.launch {
            _allowNext.emit(allow)
        }
    }

//    fun isLoggedIn() = sharedPrefs.getIsLoggedIn()
}