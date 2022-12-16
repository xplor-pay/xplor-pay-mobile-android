package com.xplore.paymobile.data.web

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.webkit.*
import androidx.core.content.ContextCompat.startActivity
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.xplore.paymobile.util.Constants.HOST_NAMES
import com.xplore.paymobile.util.SharedPreferencesDataSource
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber

@SuppressLint("SetJavaScriptEnabled")
fun setupWebView(
    webView: WebView,
    context: Context,
    jsBridgeFlows: JSBridge.JSBridgeFlows,
    onDone: () -> Unit
) {
    webView.run {
        webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                // if we are in our domain continue inside the app
                if (request?.url?.host in HOST_NAMES) {
                    return false
                }

                // if the user gesture is absent ignore the url
                if (request?.hasGesture() != true) return true

                // otherwise open a browser
                Intent(Intent.ACTION_VIEW, request.url).run {
                    startActivity(context, this, null)
                }

                return true
            }
        }

        settings.apply {
            userAgentString = "xplor_mobile"
            domStorageEnabled = true
            javaScriptEnabled = true

            allowFileAccess = false
            setGeolocationEnabled(false)
            allowContentAccess = false
        }
        addJavascriptInterface(JSBridge(context, jsBridgeFlows), "Android")
    }
    onDone()
}

class JSBridge(
    context: Context,
    private val jsBridgeFlows: JSBridgeFlows
) {

    private val sharedPrefs = SharedPreferencesDataSource(context)
    private val jsonConverter: Gson = GsonBuilder().create()
    private val backgroundScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        backgroundScope.launch {
            jsBridgeFlows.authTokenFlow.emit(sharedPrefs.getAuthToken())
            jsBridgeFlows.merchantFlow.emit(sharedPrefs.getMerchant())
            jsBridgeFlows.loggedOutFlow.emit(null)
            jsBridgeFlows.userRolesFlow.emit(sharedPrefs.getUserRoles())
        }
    }

    @JavascriptInterface
    fun authTokenUpdated(message: String) {
        backgroundScope.launch {
            Timber.d("received authTokenUpdated: $message")

            val authToken = message.toAuthToken()

            if (authToken == sharedPrefs.getAuthToken()) return@launch

            sharedPrefs.setAuthToken(message)
            jsBridgeFlows.authTokenFlow.emit(authToken)
        }
    }

    @JavascriptInterface
    fun merchantChanged(message: String) {
        backgroundScope.launch {
            Timber.d("received merchantChanged: $message")

            val merchant = message.toMerchant()

            if (merchant == sharedPrefs.getMerchant()) return@launch

            sharedPrefs.setMerchant(message)
            jsBridgeFlows.merchantFlow.emit(merchant)
        }
    }

    @JavascriptInterface
    fun userLoggedOut(message: String) {
        backgroundScope.launch {
            Timber.d("received userLoggedOut: $message")

            sharedPrefs.setAuthToken(null)

            jsBridgeFlows.loggedOutFlow.emit(message.toLoggedOut())
        }
    }

    @JavascriptInterface
    fun userRolesLoaded(message: String) {
        backgroundScope.launch {
            Timber.d("received userRolesLoaded: $message")

            val userRoles = message.toUserRoles()

            if (userRoles == sharedPrefs.getUserRoles()) return@launch

            sharedPrefs.setUserRoles(message)
            jsBridgeFlows.userRolesFlow.emit(userRoles)
        }
    }

    private fun String?.toAuthToken() = jsonConverter.fromJson(this, AuthToken::class.java)
    private fun String?.toMerchant() = jsonConverter.fromJson(this, Merchant::class.java)
    private fun String?.toLoggedOut() = jsonConverter.fromJson(this, LoggedOut::class.java)
    private fun String?.toUserRoles() = jsonConverter.fromJson(this, UserRoles::class.java)

    data class JSBridgeFlows(
        val authTokenFlow: MutableSharedFlow<AuthToken?> = MutableSharedFlow(),
        val merchantFlow: MutableSharedFlow<Merchant?> = MutableSharedFlow(),
        val loggedOutFlow: MutableSharedFlow<LoggedOut?> = MutableSharedFlow(),
        val userRolesFlow: MutableSharedFlow<UserRoles?> = MutableSharedFlow()
    )
}