package com.xplore.paymobile.data.datasource

import android.content.Context
import androidx.core.content.edit
import com.xplore.paymobile.data.remote.model.Terminal
import com.xplore.paymobile.data.web.AuthToken
import com.xplore.paymobile.data.web.Merchant
import com.xplore.paymobile.data.web.UserRoles
import com.xplore.paymobile.data.web.WebJsonConverter
import javax.inject.Inject

class SharedPreferencesDataSource @Inject constructor(
    context: Context,
    private val webJsonConverter: WebJsonConverter
) {

    companion object {
        private const val KEY_PREFERENCES = "com.xplore.paymobile.READER_PREFERENCES"
        private const val FIRST_PAIR = "FIRST_PAIR_KEY"
        private const val AUTH_TOKEN = "AUTH_TOKEN_KEY"
        private const val MERCHANT = "MERCHANT_KEY"
        private const val TERMINAL = "TERMINAL_KEY"
        private const val USER_ROLES = "USER_ROLES_KEY"
        private const val SDK_FIRST_SET_UP = "SDK_SET_UP_KEY"
    }

    private val sharedPrefs = context.getSharedPreferences(KEY_PREFERENCES, Context.MODE_PRIVATE)

    fun setFirstPair(firstPair: FirstPair) =
        sharedPrefs.edit { putInt(FIRST_PAIR, firstPair.ordinal) }

    fun getFirstPair(): FirstPair = FirstPair.fromOrdinal(retrieveFirstPair())

    fun isSdkSetUp(): Boolean = sharedPrefs.getBoolean(SDK_FIRST_SET_UP, false)

    fun sdkSetupComplete() = sharedPrefs.edit { putBoolean(SDK_FIRST_SET_UP, true) }

    fun setAuthToken(authToken: String?) = sharedPrefs.edit { putString(AUTH_TOKEN, authToken) }

    fun getAuthToken(): AuthToken? =
        sharedPrefs.getString(AUTH_TOKEN, null)?.let { webJsonConverter.jsonToAuthToken(it) }

    fun setMerchant(merchant: String) = sharedPrefs.edit { putString(MERCHANT, merchant) }

    fun setMerchant(merchant: Merchant) =
        sharedPrefs.edit { putString(MERCHANT, webJsonConverter.toJson(merchant)) }

    fun getMerchant(): Merchant? =
        sharedPrefs.getString(MERCHANT, null)?.let { webJsonConverter.jsonToMerchant(it) }

    fun clearMerchant() = sharedPrefs.edit { remove(MERCHANT) }

    fun setTerminal(terminal: Terminal) =
        sharedPrefs.edit { putString(TERMINAL, webJsonConverter.toJson(terminal)) }

    fun clearTerminal() = sharedPrefs.edit { remove(TERMINAL) }

    fun getTerminal(): Terminal? =
        sharedPrefs.getString(TERMINAL, null)?.let { webJsonConverter.jsonToTerminal(it) }

    fun setUserRoles(userRoles: String) = sharedPrefs.edit { putString(USER_ROLES, userRoles) }

    fun getUserRoles(): UserRoles? =
        sharedPrefs.getString(USER_ROLES, null)?.let { webJsonConverter.jsonToUserRoles(it) }

    private fun retrieveFirstPair(): Int =
        sharedPrefs.getInt(FIRST_PAIR, FirstPair.NOT_DONE.ordinal)

    enum class FirstPair {
        NOT_DONE, SKIPPED, DONE;

        companion object {
            fun fromOrdinal(firstPair: Int) = values().find { it.ordinal == firstPair } ?: NOT_DONE
        }
    }
}