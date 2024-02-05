package com.xplore.paymobile.ui.login

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.okta.authfoundation.claims.name
import com.okta.authfoundation.client.OidcClientResult
import com.okta.authfoundation.credential.Token
import com.okta.authfoundationbootstrap.CredentialBootstrap
import com.okta.webauthenticationui.WebAuthenticationClient.Companion.createWebAuthenticationClient
import com.xplore.paymobile.data.datasource.SharedPreferencesDataSource
import com.xplore.paymobile.data.web.GroupedUserRoles
import com.xplore.paymobile.interactiondetection.UserInteractionDetector
import com.xplore.paymobile.ui.dialog.BasicDialog
import com.xplore.paymobile.util.Constants
import com.xplore.paymobile.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject
import org.apache.commons.codec.binary.Base64

@HiltViewModel
class OktaLoginViewModel @Inject constructor(
    private val sharedPrefs: SharedPreferencesDataSource,
    private val interactionDetector: UserInteractionDetector
) : ViewModel() {

    private val className = "OktaLoginViewModel"

    private val timerScope = CoroutineScope(Dispatchers.IO)
    private var timerJob: Job? = null
    var isTokenExpired: Boolean = true

    var isLoggingIn: Boolean = false
    private val _state = MutableLiveData<BrowserState>(BrowserState.Loading)
    val state: LiveData<BrowserState> = _state

    private val base64: Base64 = Base64()

    init {
        viewModelScope.launch {
            _state.value = BrowserState.currentCredentialState()
        }
    }

    private suspend fun setTokenExpiredValue() {
        isTokenExpired = getValidToken().isNullOrBlank()
    }

    private suspend fun getValidToken(): String? {
        return CredentialBootstrap.defaultCredential().getAccessTokenIfValid()
    }

    fun login(context: Context, isRefresh: Boolean = false) {
        isLoggingIn = true
//        showNoPermissionsDialog()
//        Logger.logMessage("Attempting to login.")
        viewModelScope.launch {
            if (sharedPrefs.isLoggedIn()
                && BrowserState.currentCredentialState().equals(BrowserState.LoggedIn)
                && !isTokenExpired && !isRefresh && hasUserRolePermissionsToAccessApp())
            {
                println("Browser State = ${BrowserState.currentCredentialState()}")
                isLoggingIn = false
                sharedPrefs.setIsLoggedIn(true)
                return@launch
            }
            Logger.logMobileMessage(className, "token expired or refreshing")
            _state.value = BrowserState.Loading
            val result = CredentialBootstrap.oidcClient.createWebAuthenticationClient().login(
                context = context,
                redirectUrl = Constants.SIGN_IN_REDIRECT
            )
            handleOktaLoginResponse(result)
        }
    }

    private suspend fun handleOktaLoginResponse(
        result: OidcClientResult<Token>
    ) {
        when (result) {
            is OidcClientResult.Error -> {
                println(result.exception.message)
                Timber.e(result.exception, "Failed to login.")
                _state.value = BrowserState.currentCredentialState("Failed to login.")
            }
            is OidcClientResult.Success -> {
                val refreshToken = result.result.refreshToken
                println("refresh token: $refreshToken")
                launchOktaRefreshTimer()
                val credential = CredentialBootstrap.defaultCredential()
                credential.storeToken(token = result.result) //use to track expired token?  maybe??
                //TODO the getValidAccessToken will also refresh the token if it is expired
                val accessToken: String? = credential.getValidAccessToken()
                sharedPrefs.setAuthToken(accessToken)
                val base64Url: String? = accessToken?.split(".")?.get(1)
                val decodedBytes = base64.decode(base64Url)
                val decodedString = String(decodedBytes)
//                val decodedString = ""
                if (decodedString.isNotBlank()) {
                    try {
                        setUserInfo(decodedString)
                    } catch (e: Exception) {
                        Logger.logMobileMessage(className, "Exception occurred while setting the user info ${e.stackTrace}")
                        showNoPermissionsDialog()
                    }
                } else {
                    Logger.logMobileMessage(className, "Decoded string is blank")
                    showNoPermissionsDialog()
                }
            }
        }
        isLoggingIn = false
    }

    private suspend fun setUserInfo(
        decodedString: String
    ) {
        sharedPrefs.setUserInfo(decodedString)
        if (hasUserRolePermissionsToAccessApp()) {
            Logger.logMobileMessage(className, "User name: ${sharedPrefs.getUserName()}")
            Logger.logMobileMessage(className, "User does have permissions: ${sharedPrefs.getUserRoles()}")
            _state.value = BrowserState.LoggedIn.create()
            sharedPrefs.setIsLoggedIn(true)
            startInactivityTimer()
            setTokenExpiredValue()
        } else {
            Logger.logMobileMessage(className, "User name: ${sharedPrefs.getUserName()}")
            Logger.logMobileMessage(className, "User does not have permissions: ${sharedPrefs.getUserRoles()}")
            sharedPrefs.setIsLoggedIn(false)
            showNoPermissionsDialog()
        }
    }

    private fun showNoPermissionsDialog() {
        BasicDialog("No Access",
            "Your user does not have permission to this application. With your business owner, please contact support at 866–435–0666 for help."
        )
    }

    private fun startInactivityTimer() {
        interactionDetector.launchInactivityChecks()
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
            sharedPrefs.setIsLoggedIn(false)

            val oktaToken = CredentialBootstrap.defaultCredential().token?.idToken ?: ""
            if (oktaToken.isBlank() || BrowserState.currentCredentialState() == BrowserState.LoggedOut()) {
                println("Browser State = ${BrowserState.currentCredentialState()}")
                _state.value = BrowserState.LoggedOut()
                return@launch
            }
            val result =
                CredentialBootstrap.oidcClient.createWebAuthenticationClient().logoutOfBrowser(
                    context = context,
                    redirectUrl = Constants.LOGOUT_REDIRECT,
                    oktaToken
                )
            handleOktaLogoutResponse(result)
            isTokenExpired = true
            println("sleeping**************")
            launchOktaRefreshTimer(false)
        }
    }

    private suspend fun handleOktaLogoutResponse(result: OidcClientResult<Unit>) {
        when (result) {
            is OidcClientResult.Error -> {
                Timber.e(result.exception, "Failed to logout.")
                _state.value = BrowserState.currentCredentialState("Failed to logout.")
                println("unsuccessful logout.......")
                _state.value = BrowserState.LoggedOut()
            }
            is OidcClientResult.Success -> {
                println("successful logout.......")
                CredentialBootstrap.defaultCredential().delete()
                _state.value = BrowserState.LoggedOut()
            }
        }
    }

    private fun launchOktaRefreshTimer(startRefreshCheck: Boolean = true) {
        Timber.d("Launch Okta refresh timer")
        timerJob = timerScope.launch {
            while (true) {
                delay(1000 * 60 * 10L) // 10 minute delay
//                delay(1000 * 60 * 1L) //for testing
                Timber.d("refreshing bearer token")
                setTokenExpiredValue()
                refreshOktaToken()
            }
        }
    }

//    private fun startVTRefreshTimer() {
//        vtRefreshManager.startTimer(true)
//    }

    private suspend fun refreshOktaToken() {
        val result = CredentialBootstrap.defaultCredential().refreshToken()
        when (result) {
            is OidcClientResult.Error -> {
                println("failed to refresh")
            }
            is OidcClientResult.Success -> {
                sharedPrefs.setAuthToken(CredentialBootstrap.defaultCredential().getValidAccessToken())
                println("succeeded to refresh ${CredentialBootstrap.defaultCredential().getValidAccessToken()}")
            }
        }
        setTokenExpiredValue()
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
