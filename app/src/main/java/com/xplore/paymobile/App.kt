package com.xplore.paymobile

import android.app.Application
import com.clearent.idtech.android.wrapper.ClearentWrapper
import com.xplore.paymobile.data.datasource.EncryptedSharedPrefsDataSource
import com.xplore.paymobile.data.datasource.SharedPreferencesDataSource
import com.xplore.paymobile.util.Constants
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {

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
                ClearentWrapper.merchantHomeApiCredentials =
                    ClearentWrapper.MerchantHomeApiCredentials(
                        merchantId = merchant.merchantNumber,
                        vtToken = terminal.questJwt.token
                    )
            }
        }

        ClearentWrapper.initializeSDK(
            applicationContext,
            Constants.BASE_URL_SANDBOX,
            publicKey,
            apiKey
        )
    }
}