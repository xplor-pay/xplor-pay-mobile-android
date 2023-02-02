package com.xplore.paymobile.ui.login

import android.content.Context
import android.webkit.CookieManager
import android.webkit.WebView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xplore.paymobile.data.datasource.SharedPreferencesDataSource
import com.xplore.paymobile.data.web.JSBridge
import com.xplore.paymobile.data.web.VTRefreshManager
import com.xplore.paymobile.data.web.XplorLoginWebView
import com.xplore.paymobile.data.web.XplorLoginWebView.XplorJsCommand
import com.xplore.paymobile.interactiondetection.UserInteractionDetector
import com.xplore.paymobile.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val sharedPrefs: SharedPreferencesDataSource,
    private val vtRefreshManager: VTRefreshManager,
    val interactionDetector: UserInteractionDetector
) : ViewModel() {

    companion object {
        private val loginPageUrl = "${Constants.BASE_URL_WEB_PAGE}/ui/home"
    }

    private lateinit var xplorWebView: XplorLoginWebView

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
        xplorWebView = XplorLoginWebView(
            webView = webView,
            jsBridge = jsBridge,
            context = context
        ) {
            webView.loadUrl(loginPageUrl)
        }

        listenToCredentialsChanges()
    }

    private fun listenToCredentialsChanges() {
        viewModelScope.launch {
            sharedPrefs.merchantFlow.collect {
                if (it == null) return@collect

                xplorWebView.runJsCommand(XplorJsCommand.ChangeMerchant(it))
            }
        }
        viewModelScope.launch {
            sharedPrefs.terminalFlow.collect {
                if (it == null) return@collect

                xplorWebView.runJsCommand(XplorJsCommand.ChangeTerminal(it))
            }
        }
    }

    fun startVTRefreshTimer() {
        vtRefreshManager.startTimer(true)
    }

    fun startInactivityTimer() {
        interactionDetector.launchInactivityChecks()
    }

    fun extendSession() {
        xplorWebView.runJsCommand(XplorJsCommand.ExtendSession)
    }
}