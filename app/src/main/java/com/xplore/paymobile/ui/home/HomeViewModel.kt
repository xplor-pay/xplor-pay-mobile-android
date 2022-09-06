package com.xplore.paymobile.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.xplore.paymobile.util.EncryptedSharedPrefsDataSource
import com.xplore.paymobile.util.SharedPreferencesDataSource
import com.xplore.paymobile.util.SharedPreferencesDataSource.FirstPair
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,
    private val sharedPrefs: SharedPreferencesDataSource,
    private val encryptedPrefs: EncryptedSharedPrefsDataSource
) : AndroidViewModel(application) {

    var isCardReaderSelected = true

    fun firstPairDone() = sharedPrefs.setFirstPair(FirstPair.DONE)
    fun firstPairSkipped() = sharedPrefs.setFirstPair(FirstPair.SKIPPED)

    fun getFirstPair() = sharedPrefs.getFirstPair()
    fun shouldShowHints() = getFirstPair() == FirstPair.NOT_DONE

    fun getApiKey() = encryptedPrefs.getApiKey()
    fun getPublicKey() = encryptedPrefs.getPublicKey()
}