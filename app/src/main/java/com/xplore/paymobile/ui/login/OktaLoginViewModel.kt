package com.xplore.paymobile

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clearent.idtech.android.wrapper.ClearentWrapper
import com.okta.authfoundation.claims.name
import com.okta.authfoundation.client.OidcClientResult
import com.okta.authfoundationbootstrap.CredentialBootstrap
import com.okta.webauthenticationui.WebAuthenticationClient.Companion.createWebAuthenticationClient
import com.xplore.paymobile.data.datasource.SharedPreferencesDataSource
import com.xplore.paymobile.data.web.VTRefreshManager
import com.xplore.paymobile.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import org.apache.commons.codec.binary.Base64


@HiltViewModel
class OktaLoginViewModel @Inject constructor(
    private val vtRefreshManager: VTRefreshManager,
    private val sharedPrefs: SharedPreferencesDataSource
) : ViewModel() {

    private val clearentWrapper = ClearentWrapper.getInstance()

    var isLoggedIn: Boolean = false
    private val _state = MutableLiveData<BrowserState>(BrowserState.Loading)
    val state: LiveData<BrowserState> = _state

    private val base64: Base64 = Base64()

    init {
        viewModelScope.launch {
            _state.value = BrowserState.currentCredentialState()
        }
    }

    private suspend fun isTokenExpired(): Boolean {
        return CredentialBootstrap.defaultCredential().getAccessTokenIfValid() == null
    }

//    TODO: the jsbridge is not required anymore as it was used for merchant home.  remove the web views once the app is stable

    fun login(context: Context) {
        viewModelScope.launch {
            if (!isTokenExpired())
                return@launch

            _state.value = BrowserState.Loading
            val result = CredentialBootstrap.oidcClient.createWebAuthenticationClient().login(
                context = context,
                redirectUrl = Constants.SIGN_IN_REDIRECT
            )
            when (result) {
                is OidcClientResult.Error -> {
                    Timber.e(result.exception, "Failed to login.")
                    _state.value = BrowserState.currentCredentialState("Failed to login.")
                }
                is OidcClientResult.Success -> {
                    result.result.refreshToken
                    startVTRefreshTimer()
                    val credential = CredentialBootstrap.defaultCredential()
                    credential.storeToken(token = result.result) //use to track expired token?  maybe??
                    //TODO the getValidAccessToken will also refresh the token if it is expired
                    val accessToken: String? = credential.getValidAccessToken()
                    sharedPrefs.setAuthToken(accessToken)
                    val base64Url: String? = accessToken?.split(".")?.get(1)
                    val decodedBytes = base64.decode(base64Url)
                    val decodedString = String(decodedBytes)

                    sharedPrefs.setUserInfo(decodedString)

                    _state.value = BrowserState.LoggedIn.create()
                    isLoggedIn = true
                }
            }
        }
    }

    fun logout(context: Context) {
        viewModelScope.launch {
            _state.value = BrowserState.Loading

            val result =
                CredentialBootstrap.oidcClient.createWebAuthenticationClient().logoutOfBrowser(
                    context = context,
                    redirectUrl = Constants.LOGOUT_REDIRECT,
                    CredentialBootstrap.defaultCredential().token?.idToken ?: "",
                )
            when (result) {
                is OidcClientResult.Error -> {
                    Timber.e(result.exception, "Failed to logout.")
                    _state.value = BrowserState.currentCredentialState("Failed to logout.")
                }
                is OidcClientResult.Success -> {
                    CredentialBootstrap.defaultCredential().delete()
                    _state.value = BrowserState.LoggedOut()
                    isLoggedIn = false
                }
            }
        }
    }

    private fun startVTRefreshTimer() {
        vtRefreshManager.startTimer(true)
    }


    //TODO double check how we want to handle this
    fun hasTerminalSettings(): Boolean = clearentWrapper.getCurrentTerminalSettings() != null
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
