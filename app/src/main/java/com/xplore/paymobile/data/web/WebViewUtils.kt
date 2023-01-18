package com.xplore.paymobile.data.web

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.webkit.*
import androidx.core.content.ContextCompat.startActivity
import com.xplore.paymobile.data.datasource.SharedPreferencesDataSource
import com.xplore.paymobile.util.Constants
import com.xplore.paymobile.util.Constants.HOST_NAMES
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.net.HttpURLConnection.HTTP_FORBIDDEN
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
import javax.inject.Inject

@SuppressLint("SetJavaScriptEnabled")
fun setupWebView(
    webView: WebView,
    context: Context,
    jsBridge: JSBridge,
    onDone: () -> Unit
) {
    webView.run {
        webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                // if we are detecting a logout event
                if (request?.url.toString().contains(Constants.SIGNOUT_WEB_PAGE_URL)
                ) {
                    jsBridge.logout()
                    return false
                }

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

            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                super.onReceivedHttpError(view, request, errorResponse)

                val statusCode = errorResponse?.statusCode
                if (statusCode == HTTP_UNAUTHORIZED || statusCode == HTTP_FORBIDDEN) {
                    jsBridge.logout()
                }
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
        addJavascriptInterface(jsBridge, "Android")
    }
    onDone()
}

class JSBridge @Inject constructor(
    val jsBridgeFlows: JSBridgeFlows,
    private val webJsonConverter: WebJsonConverter,
    private val sharedPrefs: SharedPreferencesDataSource
) {
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

            val authToken = webJsonConverter.jsonToAuthToken(message)

            if (authToken == sharedPrefs.getAuthToken()) return@launch

            sharedPrefs.setAuthToken(message)
            jsBridgeFlows.authTokenFlow.emit(authToken)
        }
    }

    @JavascriptInterface
    fun merchantChanged(message: String) {
        backgroundScope.launch {
            Timber.d("received merchantChanged: $message")

            val merchant = webJsonConverter.jsonToMerchant(message)

            if (merchant == sharedPrefs.getMerchant()) return@launch

            sharedPrefs.setMerchant(message)
            jsBridgeFlows.merchantFlow.emit(merchant)
        }
    }

    @JavascriptInterface
    fun userLoggedOut(message: String) {
        backgroundScope.launch {
            Timber.d("received userLoggedOut: $message")

            jsBridgeFlows.loggedOutFlow.emit(webJsonConverter.jsonToLoggedOut(message))
        }
    }

    @JavascriptInterface
    fun userRolesLoaded(message: String) {
        backgroundScope.launch {
            Timber.d("received userRolesLoaded: $message")

            val userRoles = webJsonConverter.jsonToUserRoles(message)

            if (userRoles == sharedPrefs.getUserRoles()) return@launch

            sharedPrefs.setUserRoles(message)
            jsBridgeFlows.userRolesFlow.emit(userRoles)
        }
    }

    fun logout() = backgroundScope.launch { jsBridgeFlows.loggedOutFlow.emit(LoggedOut(true)) }

    data class JSBridgeFlows(
        val authTokenFlow: MutableSharedFlow<AuthToken?> = MutableSharedFlow(),
        val merchantFlow: MutableSharedFlow<Merchant?> = MutableSharedFlow(),
        val loggedOutFlow: MutableSharedFlow<LoggedOut?> = MutableSharedFlow(),
        val userRolesFlow: MutableSharedFlow<UserRoles?> = MutableSharedFlow()
    )
}