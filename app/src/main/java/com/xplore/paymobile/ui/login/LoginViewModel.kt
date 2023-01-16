package com.xplore.paymobile.ui.login

import android.content.Context
import android.webkit.CookieManager
import android.webkit.WebView
import androidx.lifecycle.ViewModel
import com.xplore.paymobile.data.datasource.SharedPreferencesDataSource
import com.xplore.paymobile.data.web.JSBridge
import com.xplore.paymobile.data.web.setupWebView
import com.xplore.paymobile.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val sharedPrefs: SharedPreferencesDataSource,
) : ViewModel() {

    companion object {
        private val loginPageUrl = "${Constants.BASE_URL_WEB_PAGE}/ui/home"
    }

    var onLoginSuccessful: () -> Unit = {}

    fun prepareWebView(webView: WebView, context: Context, jsBridge: JSBridge) {
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