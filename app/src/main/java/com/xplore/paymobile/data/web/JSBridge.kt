package com.xplore.paymobile.data.web

import android.webkit.JavascriptInterface
import com.xplore.paymobile.data.datasource.SharedPreferencesDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class JSBridge @Inject constructor(
    val jsBridgeFlows: JSBridgeFlows,
    private val webJsonConverter: WebJsonConverter,
    private val sharedPrefs: SharedPreferencesDataSource
) {
    private val backgroundScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var shouldRefreshUserRoles = false
    private var lastAuthTokenUsed = ""

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

            if (lastAuthTokenUsed != message) {
                shouldRefreshUserRoles = true
                lastAuthTokenUsed = message
            }

            val authToken = webJsonConverter.jsonToAuthToken(message)

            if (authToken == sharedPrefs.getAuthToken()) return@launch

            sharedPrefs.setAuthToken(message)
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

            if (!shouldRefreshUserRoles) return@launch

            shouldRefreshUserRoles = false
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