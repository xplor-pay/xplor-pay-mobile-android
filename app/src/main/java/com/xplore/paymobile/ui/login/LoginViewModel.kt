package com.xplore.paymobile.ui.login

import android.content.Context
import android.webkit.WebView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xplore.paymobile.data.web.JSBridge
import com.xplore.paymobile.data.web.setupWebView
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val jsBridge: JSBridge
) : ViewModel() {

    companion object {
        private const val loginPageUrl = "https://my-qa.clearent.net/ui/home"
    }

    var onLoginSuccessful: () -> Unit = {}
    private val _loginEventsFlow = MutableSharedFlow<LoginEvents>()
    val loginEventsFlow: SharedFlow<LoginEvents> = _loginEventsFlow

    init {
        viewModelScope.launch {
            jsBridge.jsBridgeFlows.authTokenFlow.collectLatest { authToken ->
                authToken?.also {
                    _loginEventsFlow.emit(LoginEvents.LoginSuccessful)
                }
            }
        }
    }

    fun prepareWebView(webView: WebView, context: Context) {
        setupWebView(webView, context, jsBridge) {
            webView.loadUrl(loginPageUrl)
        }
    }

    sealed class LoginEvents {
        object LoginSuccessful : LoginEvents()
    }
}