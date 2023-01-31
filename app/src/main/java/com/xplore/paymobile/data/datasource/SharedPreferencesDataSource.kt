package com.xplore.paymobile.data.datasource

import android.content.Context
import androidx.core.content.edit
import com.auth0.android.jwt.JWT
import com.xplore.paymobile.data.remote.model.Terminal
import com.xplore.paymobile.data.web.AuthToken
import com.xplore.paymobile.data.web.Merchant
import com.xplore.paymobile.data.web.UserRoles
import com.xplore.paymobile.data.web.WebJsonConverter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class SharedPreferencesDataSource @Inject constructor(
    context: Context,
    private val webJsonConverter: WebJsonConverter
) {

    companion object {
        private const val KEY_PREFERENCES = "com.xplore.paymobile.READER_PREFERENCES"
        private const val FIRST_PAIR = "FIRST_PAIR_KEY"
        private const val AUTH_TOKEN = "AUTH_TOKEN_KEY"
        private const val MERCHANT_BASE = "MERCHANT_KEY_"
        private const val TERMINAL_BASE = "TERMINAL_KEY_"
        private const val USER_ROLES = "USER_ROLES_KEY"
        private const val SDK_FIRST_SET_UP = "SDK_SET_UP_KEY"

        private const val CLIENT_ID_CLAIM = "client_id"
    }

    private val sharedPrefs = context.getSharedPreferences(KEY_PREFERENCES, Context.MODE_PRIVATE)

    private val backgroundScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _merchantFlow = MutableSharedFlow<Merchant?>()
    val merchantFlow: SharedFlow<Merchant?> = _merchantFlow
    private val _terminalFlow = MutableSharedFlow<Terminal?>()
    val terminalFlow: SharedFlow<Terminal?> = _terminalFlow

    init {
        backgroundScope.launch {
            _merchantFlow.emit(getMerchant())
            _terminalFlow.emit(getTerminal())
        }
    }

    fun setFirstPair(firstPair: FirstPair) =
        sharedPrefs.edit { putInt(FIRST_PAIR, firstPair.ordinal) }

    fun getFirstPair(): FirstPair = FirstPair.fromOrdinal(retrieveFirstPair())

    fun isSdkSetUp(): Boolean = sharedPrefs.getBoolean(SDK_FIRST_SET_UP, false)

    fun sdkSetupComplete() = sharedPrefs.edit { putBoolean(SDK_FIRST_SET_UP, true) }

    fun setAuthToken(authToken: String?) = backgroundScope.launch {
        sharedPrefs.edit { putString(AUTH_TOKEN, authToken) }

        _merchantFlow.emit(getMerchant())
        _terminalFlow.emit(getTerminal())
    }

    fun getAuthToken(): AuthToken? =
        sharedPrefs.getString(AUTH_TOKEN, null)?.let { webJsonConverter.jsonToAuthToken(it) }

    fun setMerchant(merchant: String) = backgroundScope.launch {
        sharedPrefs.edit { putString(MERCHANT_BASE + getClientIdFromAuthToken(), merchant) }
        clearTerminal()
        _merchantFlow.emit(webJsonConverter.jsonToMerchant(merchant))
    }

    fun setMerchant(merchant: Merchant) = backgroundScope.launch {
        sharedPrefs.edit {
            putString(
                MERCHANT_BASE + getClientIdFromAuthToken(),
                webJsonConverter.toJson(merchant)
            )
        }
        clearTerminal()
        _merchantFlow.emit(merchant)
    }

    fun getMerchant(): Merchant? =
        sharedPrefs.getString(MERCHANT_BASE + getClientIdFromAuthToken(), null)
            ?.let { webJsonConverter.jsonToMerchant(it) }

    fun clearMerchant() = backgroundScope.launch {
        sharedPrefs.edit { remove(MERCHANT_BASE + getClientIdFromAuthToken()) }
        _merchantFlow.emit(null)
    }

    fun setTerminal(terminal: Terminal) = backgroundScope.launch {
        sharedPrefs.edit {
            putString(
                TERMINAL_BASE + getClientIdFromAuthToken(),
                webJsonConverter.toJson(terminal)
            )
        }
        _terminalFlow.emit(terminal)
    }

    fun clearTerminal() = backgroundScope.launch {
        sharedPrefs.edit { remove(TERMINAL_BASE + getClientIdFromAuthToken()) }
        _terminalFlow.emit(null)
    }

    fun getTerminal(): Terminal? =
        sharedPrefs.getString(TERMINAL_BASE + getClientIdFromAuthToken(), null)
            ?.let { webJsonConverter.jsonToTerminal(it) }

    fun setUserRoles(userRoles: String) = sharedPrefs.edit { putString(USER_ROLES, userRoles) }

    fun getUserRoles(): UserRoles? =
        sharedPrefs.getString(USER_ROLES, null)?.let { webJsonConverter.jsonToUserRoles(it) }

    private fun getClientIdFromAuthToken(): String? =
        getAuthToken()?.bearerToken?.let {
            JWT(formatBearerToken(it)).getClaim(CLIENT_ID_CLAIM).asString()
        }

    private fun formatBearerToken(bearerToken: String) = bearerToken.drop(7)

    private fun retrieveFirstPair(): Int =
        sharedPrefs.getInt(FIRST_PAIR, FirstPair.NOT_DONE.ordinal)

    enum class FirstPair {
        NOT_DONE, SKIPPED, DONE;

        companion object {
            fun fromOrdinal(firstPair: Int) = values().find { it.ordinal == firstPair } ?: NOT_DONE
        }
    }
}