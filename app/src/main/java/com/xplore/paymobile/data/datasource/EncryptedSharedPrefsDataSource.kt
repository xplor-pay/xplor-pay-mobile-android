package com.xplore.paymobile.data.datasource

import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class EncryptedSharedPrefsDataSource(context: Context) {

    companion object {
        private const val KEY_PREFERENCES = "ENCRYPTED_SHARED_PREFS"
        private const val VT_TOKEN = "VT_TOKEN"
//        private const val PUBLIC_KEY = "PUBLIC_KEY"
        private const val PASSPHRASE_KEY = "PASSPHRASE_KEY"
    }

    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val sharedPrefs = EncryptedSharedPreferences.create(
        KEY_PREFERENCES,
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    //    private var spec: KeyGenParameterSpec = MasterKey.Builder(
//        KEY_PREFERENCES,
//        KeyProperties.PURPOSE_ENCRYPT.toString()
//    )
//        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
//        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
//        .setKeySize(KEY_SIZE)
//        .build()
//
//    var masterKey = MasterKey.Builder(this@MainActivity)
//        .setKeyGenParameterSpec(spec)
//        .build()
//    private val sharedPrefs = EncryptedSharedPreferences.create(
//        KEY_PREFERENCES,
//        masterKeyAlias,
//        context,
//        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
//        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
//    )

    fun setDbPassphrase(passphrase: String) =
        sharedPrefs.edit { putString(PASSPHRASE_KEY, passphrase) }

    fun getDbPassphrase(): String = sharedPrefs.getString(PASSPHRASE_KEY, "") ?: ""

    fun setVtToken(apiKey: String) =
        sharedPrefs.edit { putString(VT_TOKEN, apiKey) }

    fun getVtToken(): String = sharedPrefs.getString(VT_TOKEN, "") ?: ""

//    fun setApiKey(apiKey: String) =
//        sharedPrefs.edit { putString(API_KEY, apiKey) }
//
//    fun getApiKey(): String = sharedPrefs.getString(API_KEY, "") ?: ""
//
//    fun setPublicKey(publicKey: String) =
//        sharedPrefs.edit { putString(PUBLIC_KEY, publicKey) }
//
//    fun getPublicKey(): String = sharedPrefs.getString(PUBLIC_KEY, "") ?: ""
}