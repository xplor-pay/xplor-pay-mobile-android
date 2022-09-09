package com.xplore.paymobile.ui.more

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.xplore.paymobile.util.EncryptedSharedPrefsDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MoreViewModel @Inject constructor(
    application: Application,
    private val encryptedPrefs: EncryptedSharedPrefsDataSource
) : AndroidViewModel(application) {

    fun setApiKey(apiKey: String) = encryptedPrefs.setApiKey(apiKey)

    fun getApiKey() = encryptedPrefs.getApiKey()

    fun setPublicKey(publicKey: String) = encryptedPrefs.setPublicKey(publicKey)

    fun getPublicKey() = encryptedPrefs.getPublicKey()

}