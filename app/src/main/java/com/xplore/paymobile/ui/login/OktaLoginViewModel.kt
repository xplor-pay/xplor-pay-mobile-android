package com.xplore.paymobile.ui.login

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.okta.authfoundation.claims.name
import com.okta.authfoundation.client.OidcClientResult
import com.okta.authfoundationbootstrap.CredentialBootstrap
import com.okta.webauthenticationui.WebAuthenticationClient.Companion.createWebAuthenticationClient
import com.xplore.paymobile.MainActivity
import com.xplore.paymobile.data.datasource.SharedPreferencesDataSource
import com.xplore.paymobile.data.web.GroupedUserRoles
import com.xplore.paymobile.ui.dialog.BasicDialog
import com.xplore.paymobile.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import org.apache.commons.codec.binary.Base64

//todo could we use a webview to login?  not a fan of destroying and rerending the home page
@HiltViewModel
class OktaLoginViewModel @Inject constructor(
    private val sharedPrefs: SharedPreferencesDataSource
) : ViewModel() {

//    private val timerScope = CoroutineScope(Dispatchers.IO)
//    private var timerJob: Job? = null

//    private val clearentWrapper = ClearentWrapper.getInstance()

    var isLoggingIn: Boolean = false
    private val _state = MutableLiveData<BrowserState>(BrowserState.Loading)
    val state: LiveData<BrowserState> = _state

    private val base64: Base64 = Base64()

    init {
        viewModelScope.launch {
            _state.value = BrowserState.currentCredentialState()
        }
    }

    suspend fun isTokenExpired(): Boolean {
        return CredentialBootstrap.defaultCredential().getAccessTokenIfValid().isNullOrBlank()
    }

//    TODO: the jsbridge is not required anymore as it was used for merchant home.  remove the web views once the app is stable

    fun login(context: Context) {
        isLoggingIn = true
//        Logger.logMessage("Attempting to login.")
//        sharedPrefs.setIsLoggedIn(false)
        viewModelScope.launch {
            if (sharedPrefs.isLoggedIn()
                && BrowserState.currentCredentialState().equals(BrowserState.LoggedIn))
            {
                println("Browser State = ${BrowserState.currentCredentialState()}")
                isLoggingIn = false
                sharedPrefs.setIsLoggedIn(true)
                isLoggingIn = false
                return@launch
            }
            println("===============token expired")
            _state.value = BrowserState.Loading
            val result = CredentialBootstrap.oidcClient.createWebAuthenticationClient().login(
                context = context,
                redirectUrl = Constants.SIGN_IN_REDIRECT
            )

            when (result) {
                is OidcClientResult.Error -> {
                    println(result.exception.message)
//                    val errorMessage = result.exception.message
//                    if (!errorMessage.isNullOrBlank()) {
//                        println("***************************")
//                        _state.value = BrowserState.currentCredentialState(errorMessage)
//                    } else {
                        Timber.e(result.exception, "Failed to login.")
                        _state.value = BrowserState.currentCredentialState("Failed to login.")
//                    }
                }
                is OidcClientResult.Success -> {
                    result.result.refreshToken
                    val credential = CredentialBootstrap.defaultCredential()
                    credential.storeToken(token = result.result) //use to track expired token?  maybe??
                    //TODO the getValidAccessToken will also refresh the token if it is expired
                    val accessToken: String? = credential.getValidAccessToken()
                    sharedPrefs.setAuthToken(accessToken)
                    val base64Url: String? = accessToken?.split(".")?.get(1)
                    val decodedBytes = base64.decode(base64Url)
                    val decodedString = String(decodedBytes)

                    sharedPrefs.setUserInfo(decodedString)
                    //todo need to figure out the okta refresh timer
//                    launchOktaRefreshTimer()
                    if (hasUserRolePermissionsToAccessApp()) {
                        _state.value = BrowserState.LoggedIn.create()
                        sharedPrefs.setIsLoggedIn(true)
                    } else {
                        logout(context)
                    }
                    isLoggingIn = false
                }
            }
        }
    }

    private fun hasUserRolePermissionsToAccessApp(): Boolean {
        return when (GroupedUserRoles.fromString(sharedPrefs.getUserRoles())) {
            GroupedUserRoles.NoAccess -> false
            else -> {
                true
            }
        }
    }


    fun logout(context: Context) {

        viewModelScope.launch {
//            val authToken = sharedPrefs.getAuthToken()
//            authToken?.let { CredentialBootstrap.oidcClient.revokeToken(it) }
//            _state.value = BrowserState.LoggedOut()
//            sharedPrefs.setAuthToken(null)
            sharedPrefs.setIsLoggedIn(false)
//
//            if (isTokenExpired()) {
//                println("((((((((((((((((((((((((((((((((token exprired")
//                return@launch
//            }
//
//            if (isTokenExpired()) {
//                println("****************token expired")
//                login(context)
//            }
//            CredentialBootstrap.defaultCredential().delete()
//            sharedPrefs.setIsLoggedIn(false)
//            sharedPrefs.setAuthToken(null)
//            _state.value = BrowserState.LoggedOut()
            if (isLoggingIn) {
                return@launch
            }
            val oktaToken = CredentialBootstrap.defaultCredential().token?.idToken ?: ""
            if (oktaToken.isBlank() || BrowserState.currentCredentialState() == BrowserState.LoggedOut()) {
                println("Browser State = ${BrowserState.currentCredentialState()}")
//                sharedPrefs.setAuthToken(null)
                _state.value = BrowserState.LoggedOut()
//                sharedPrefs.setIsLoggedIn(false)
                return@launch
            }
            val result =
                CredentialBootstrap.oidcClient.createWebAuthenticationClient().logoutOfBrowser(
                    context = context,
                    redirectUrl = Constants.LOGOUT_REDIRECT,
                    oktaToken
                )
            when (result) {
                is OidcClientResult.Error -> {
                    Timber.e(result.exception, "Failed to logout.")
                    _state.value = BrowserState.currentCredentialState("Failed to logout.")
                    println("unsuccessful logout.......")
//                    sharedPrefs.setAuthToken(null)
                    _state.value = BrowserState.LoggedOut()
//                    sharedPrefs.setIsLoggedIn(false)
                }
                is OidcClientResult.Success -> {
                    println("successful logout.......")
                    CredentialBootstrap.defaultCredential().delete()
//                    sharedPrefs.setAuthToken(null)
                    _state.value = BrowserState.LoggedOut()
                    sharedPrefs.setIsLoggedIn(false)
                }
            }
            delay(500L)
        }
    }

//    private fun launchOktaRefreshTimer() {
//        Timber.d("Launch Okta refresh timer")
//        timerJob = timerScope.launch {
//            while (true) {
//                delay(60000L)
//                refreshOktaToken()
//            }
//        }
//    }

//    private fun startVTRefreshTimer() {
//        vtRefreshManager.startTimer(true)
//    }

    private suspend fun refreshOktaToken() {
//        if (currentCredentialState() == BrowserState.LoggedOut
//        )
        val credential = CredentialBootstrap.defaultCredential()
        var token = credential.token?.accessToken
        if (token.isNullOrBlank()) {
            token = credential.getValidAccessToken()
        }
        Timber.d("bearer token before refresh $token")
        val result = token?.let { CredentialBootstrap.oidcClient.refreshToken(it) }
        Timber.d("after refresh ${result.toString()}")
//        launchOktaRefreshTimer()

//        val result = CredentialBootstrap.oidcClient.createWebAuthenticationClient().login(
//            context = context,
//            redirectUrl = Constants.SIGN_IN_REDIRECT
//        )
//        when (result) {
//            is OidcClientResult.Error -> {
//                println(result.exception.message)
//                val errorMessage = result.exception.message
//                if (!errorMessage.isNullOrBlank() && errorMessage == "Flow cancelled.") {
//                    println("***************************")
////                    _state.value = BrowserState.currentCredentialState(errorMessage)
//                } else {
//                    Timber.e(result.exception, "Failed to login.")
////                    _state.value = BrowserState.currentCredentialState("Failed to login.")
//                }
//            }
//            is OidcClientResult.Success -> {
//                val credential = CredentialBootstrap.defaultCredential()
//                credential.storeToken(token = result.result) //use to track expired token?  maybe??
//                //TODO the getValidAccessToken will also refresh the token if it is expired
//                val accessToken: String? = credential.getValidAccessToken()
//                sharedPrefs.setAuthToken(accessToken)
//                val base64Url: String? = accessToken?.split(".")?.get(1)
//                val decodedBytes = base64.decode(base64Url)
//                val decodedString = String(decodedBytes)
//
//                sharedPrefs.setUserInfo(decodedString)
//                sharedPrefs.setIsLoggedIn(true)
//
////                _state.value = BrowserState.LoggedIn.create()
//            }
//        }
    }
}

sealed class BrowserState {
    object Loading : BrowserState()
    class LoggedOut(val errorMessage: String? = null) : BrowserState()
    class LoggedIn private constructor(
        val name: String,
        val errorMessage: String?
    ) : BrowserState() {
        companion object {
            suspend fun create(errorMessage: String? = null): BrowserState {
                val credential = CredentialBootstrap.defaultCredential()
                val name = credential.idToken()?.name ?: ""
                return LoggedIn(name, errorMessage)
            }
        }
    }

    companion object {
        suspend fun currentCredentialState(errorMessage: String? = null): BrowserState {
            val credential = CredentialBootstrap.defaultCredential()
            return if (credential.token == null) {
                LoggedOut(errorMessage)
            } else {
                LoggedIn.create(errorMessage)
            }
        }
    }

}
