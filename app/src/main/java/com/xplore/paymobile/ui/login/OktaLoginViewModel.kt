package com.xplore.paymobile

import android.content.Context
import androidx.lifecycle.*
import com.okta.authfoundation.claims.name
import com.okta.authfoundation.client.OidcClientResult
import com.okta.authfoundationbootstrap.CredentialBootstrap
import com.okta.webauthenticationui.WebAuthenticationClient.Companion.createWebAuthenticationClient
import com.xplore.paymobile.data.datasource.RemoteDataSource
import com.xplore.paymobile.data.datasource.SharedPreferencesDataSource
import com.xplore.paymobile.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class OktaViewModel
//@Inject constructor(
//    private val remoteDataSource: RemoteDataSource)
    : ViewModel() {

    @Inject
    lateinit var remoteDataSource: RemoteDataSource

    var isLoggedIn: Boolean = false
    private val _state = MutableLiveData<BrowserState>(BrowserState.Loading)
    val state: LiveData<BrowserState> = _state

    init {
        viewModelScope.launch {
            _state.value = BrowserState.currentCredentialState()
        }
    }

//todo
    fun login(context: Context): String {
        var accessToken = ""
        viewModelScope.launch {
            _state.value = BrowserState.Loading
            withContext(Dispatchers.IO) {
                Thread.sleep(1000)
            }

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
                    val credential = CredentialBootstrap.defaultCredential()
                    credential.storeToken(token = result.result)
                    _state.value = BrowserState.LoggedIn.create()
                    isLoggedIn = true
//                    val jwt: Jwt = JwtParser.parse(result.result.accessToken)
                    accessToken = "Bearer " + result.result.accessToken
//                    println("*****************************")
                    getAuthTokenFromAccessToken(accessToken)
//                    println(result.result.accessToken)
//                    println(result.result.idToken)
//                    println(result.result.expiresIn)
                }
            }
        }
        return accessToken
    }

    private suspend fun getAuthTokenFromAccessToken(accessToken: String) {
        var authToken: Unit = remoteDataSource.getEdgeToken(accessToken)
        println(authToken)
    }

    fun logoutOfBrowser(context: Context) {
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
