package com.xplore.paymobile.data.web

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
open class WebEventsSharedViewModel @Inject constructor(
    val jsBridge: JSBridge
) : ViewModel() {

    var allowLogout = true

    private val _loginEventsFlow = MutableSharedFlow<LoginEvents>()
    val loginEventsFlow: SharedFlow<LoginEvents> = _loginEventsFlow

    init {
        viewModelScope.launch {
            jsBridge.jsBridgeFlows.loggedOutFlow.collectLatest { loggedOut ->
                if (allowLogout) {
                    allowLogout = false
                    loggedOut?.also {
                        _loginEventsFlow.emit(LoginEvents.Logout)
                    }
                }
            }
        }

        viewModelScope.launch {
            jsBridge.jsBridgeFlows.authTokenFlow.collectLatest { authToken ->
                authToken?.also {
                    _loginEventsFlow.emit(LoginEvents.LoginSuccessful)
                }
            }
        }
    }
}

sealed class LoginEvents {
    object LoginSuccessful : LoginEvents()
    object Logout : LoginEvents()
}