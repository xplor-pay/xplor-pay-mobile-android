package com.xplore.paymobile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xplore.paymobile.data.web.JSBridge
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
open class ActivityViewModel @Inject constructor(
    private val jsBridge: JSBridge
) : ViewModel() {

    protected val _loginEventsFlow = MutableSharedFlow<LoginEvents>()
    val loginEventsFlow: SharedFlow<LoginEvents> = _loginEventsFlow

    init {
        viewModelScope.launch {
            jsBridge.jsBridgeFlows.loggedOutFlow.collectLatest { loggedOut ->
                loggedOut?.also {
                    _loginEventsFlow.emit(LoginEvents.Logout)
                }
            }
        }
    }
}

sealed class LoginEvents {
    object LoginSuccessful : LoginEvents()
    object Logout : LoginEvents()
}