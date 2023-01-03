package com.xplore.paymobile.data.datasource

import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class EncryptedSharedPrefsDataSource(context: Context) {

    companion object {
        private const val KEY_PREFERENCES = "ENCRYPTED_SHARED_PREFS"
        private const val API_KEY = "API_KEY"
        private const val PUBLIC_KEY = "PUBLIC_KEY"
    }

    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val sharedPrefs = EncryptedSharedPreferences.create(
        KEY_PREFERENCES,
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun setApiKey(apiKey: String) =
        sharedPrefs.edit { putString(API_KEY, apiKey) }

    fun getApiKey(): String = sharedPrefs.getString(API_KEY, "") ?: ""

    fun setPublicKey(publicKey: String) =
        sharedPrefs.edit { putString(PUBLIC_KEY, publicKey) }

    fun getPublicKey(): String = sharedPrefs.getString(PUBLIC_KEY, "") ?: ""
}