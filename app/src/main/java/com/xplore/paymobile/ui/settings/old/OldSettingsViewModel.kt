package com.xplore.paymobile.ui.settings.old

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.xplore.paymobile.data.datasource.EncryptedSharedPrefsDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OldSettingsViewModel @Inject constructor(
    application: Application,
    private val encryptedPrefs: EncryptedSharedPrefsDataSource
) : AndroidViewModel(application) {

    fun setApiKey(apiKey: String) = encryptedPrefs.setApiKey(apiKey)

    fun getApiKey() = encryptedPrefs.getApiKey()

    fun setPublicKey(publicKey: String) = encryptedPrefs.setPublicKey(publicKey)

    fun getPublicKey() = encryptedPrefs.getPublicKey()

}