package com.xplore.paymobile

import android.app.Application
import com.clearent.idtech.android.wrapper.ClearentWrapper
import com.clearent.idtech.android.wrapper.offline.config.OfflineModeConfig
import com.xplore.paymobile.data.datasource.EncryptedSharedPrefsDataSource
import com.xplore.paymobile.data.datasource.SharedPreferencesDataSource
import com.xplore.paymobile.util.Constants
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {

    private val clearentWrapper = ClearentWrapper.getInstance()

    @Inject
    lateinit var encryptedPrefs: EncryptedSharedPrefsDataSource

    @Inject
    lateinit var sharedPreferencesDataSource: SharedPreferencesDataSource

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        logoutWebView()

        encryptedPrefs = EncryptedSharedPrefsDataSource(applicationContext)
        initSdkWrapper()
    }

    private fun logoutWebView() {
        sharedPreferencesDataSource.setAuthToken(null)
    }

    private fun initSdkWrapper() {
        val apiKey = encryptedPrefs.getApiKey()
        val publicKey = resources.getString(R.string.public_key)

        sharedPreferencesDataSource.getMerchant()?.also { merchant ->
            sharedPreferencesDataSource.getTerminal()?.also { terminal ->
                clearentWrapper.merchantHomeApiCredentials =
                    ClearentWrapper.MerchantHomeApiCredentials(
                        merchantId = merchant.merchantNumber,
                        vtToken = terminal.questJwt.token
                    )
            }
        }

        clearentWrapper.initializeSDK(
            applicationContext,
            Constants.BASE_URL_SANDBOX,
            publicKey,
            apiKey,
            //TODO proper key management
            OfflineModeConfig("PassPhrase")
        )

        // set up the sdk store and forward mode once so we don't override user preferences
        if (sharedPreferencesDataSource.isSdkSetUp())
            return

        sharedPreferencesDataSource.sdkSetupComplete()
    }
}