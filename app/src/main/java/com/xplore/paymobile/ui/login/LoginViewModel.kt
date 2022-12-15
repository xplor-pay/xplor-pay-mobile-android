package com.xplore.paymobile.ui.login

import android.content.Context
import android.webkit.WebView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xplore.paymobile.data.web.JSBridge
import com.xplore.paymobile.data.web.setupWebView
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    companion object {
        private const val loginPageUrl = "https://my-qa.clearent.net/ui/home"
    }

    private val jsBridgeFlows = JSBridge.JSBridgeFlows()

    private val _loginEventsFlow = MutableSharedFlow<LoginEvents>()
    val loginEventsFlow: SharedFlow<LoginEvents> = _loginEventsFlow

    init {
        viewModelScope.launch {
            jsBridgeFlows.authTokenFlow.collectLatest { authToken ->
                authToken?.also {
                    _loginEventsFlow.emit(LoginEvents.LoginSuccessful)
                }
            }
        }
    }

    fun prepareWebView(webView: WebView, context: Context) {
        setupWebView(webView, context, jsBridgeFlows) {
            webView.loadUrl(loginPageUrl)
        }
    }

    sealed class LoginEvents {
        object LoginSuccessful : LoginEvents()
    }
}