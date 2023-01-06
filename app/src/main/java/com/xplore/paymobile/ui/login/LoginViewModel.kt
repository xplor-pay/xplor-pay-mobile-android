package com.xplore.paymobile.ui.login

import android.content.Context
import android.webkit.WebView
import androidx.lifecycle.viewModelScope
import com.xplore.paymobile.data.web.JSBridge
import com.xplore.paymobile.data.web.setupWebView
import dagger.hilt.android.lifecycle.HiltViewModel
import android.webkit.CookieManager
import com.xplore.paymobile.ActivityViewModel
import com.xplore.paymobile.LoginEvents
import com.xplore.paymobile.data.datasource.SharedPreferencesDataSource
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val jsBridge: JSBridge,
    private val sharedPrefs: SharedPreferencesDataSource,
) : ActivityViewModel(jsBridge) {

    companion object {
        private const val loginPageUrl = "https://my-qa.clearent.net/ui/home"
    }

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
        sharedPrefs.getAuthToken() ?: run {
            // Clear all the cookies
            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().flush()

            webView.clearCache(true)
            webView.clearFormData()
            webView.clearHistory()
            webView.clearSslPreferences()
        }
        setupWebView(webView, context, jsBridge) {
            webView.loadUrl(loginPageUrl)
        }
    }
}