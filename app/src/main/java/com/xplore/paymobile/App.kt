package com.xplore.paymobile

import android.app.Application
import com.clearent.idtech.android.wrapper.SDKWrapper
import com.xplore.paymobile.util.Constants
import com.xplore.paymobile.util.EncryptedSharedPrefsDataSource
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class App : Application() {

    private lateinit var encryptedPrefs: EncryptedSharedPrefsDataSource

    override fun onCreate() {
        super.onCreate()
        encryptedPrefs = EncryptedSharedPrefsDataSource(applicationContext)
        initSdkWrapper()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    private fun initSdkWrapper() {
        val apiKey = encryptedPrefs.getApiKey()
        val publicKey = encryptedPrefs.getPublicKey()
        SDKWrapper.initializeReader(
            applicationContext,
            Constants.BASE_URL_SANDBOX,
            publicKey,
            apiKey
        )
    }
}