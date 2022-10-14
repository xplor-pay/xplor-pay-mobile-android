package com.xplore.paymobile

import android.app.Application
import com.clearent.idtech.android.wrapper.ClearentWrapper
import com.clearent.idtech.android.wrapper.offline.model.StoreAndForwardMode
import com.xplore.paymobile.util.Constants
import com.xplore.paymobile.util.EncryptedSharedPrefsDataSource
import com.xplore.paymobile.util.SharedPreferencesDataSource
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {

    private val clearentWrapper = ClearentWrapper.getInstance()

    @Inject
    lateinit var encryptedPrefs: EncryptedSharedPrefsDataSource

    @Inject
    lateinit var sharedPrefs: SharedPreferencesDataSource

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        initSdkWrapper()
    }

    private fun initSdkWrapper() {
        val apiKey = encryptedPrefs.getApiKey()
        val publicKey = encryptedPrefs.getPublicKey()
        clearentWrapper.initializeSDK(
            applicationContext,
            Constants.BASE_URL_SANDBOX,
            publicKey,
            apiKey
        )

        // set up the sdk store and forward mode once so we don't override user preferences
        if (sharedPrefs.isSdkSetUp())
            return

        clearentWrapper.storeAndForwardEnabled = true
        clearentWrapper.storeAndForwardMode = StoreAndForwardMode.PROMPT
        sharedPrefs.sdkSetupComplete()
    }
}