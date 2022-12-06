package com.xplore.paymobile

import android.app.Application
import com.clearent.idtech.android.wrapper.ClearentWrapper
import com.xplore.paymobile.data.datasource.RemoteDataSource
import com.xplore.paymobile.util.Constants
import com.xplore.paymobile.util.EncryptedSharedPrefsDataSource
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {

    private lateinit var encryptedPrefs: EncryptedSharedPrefsDataSource

    @Inject
    lateinit var remoteDataSource: RemoteDataSource

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        encryptedPrefs = EncryptedSharedPrefsDataSource(applicationContext)
        initSdkWrapper()

        // TODO: remove this after done, used only for test purposes
        foo()
    }

    private fun foo() = CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
        remoteDataSource.authToken = "manually entered auth since there is no login yet"
//        remoteDataSource.searchMerchants(SearchMerchantOptions(null, "1", "10"))
//        remoteDataSource.getMerchantDetails("6588000000610659")
        remoteDataSource.fetchTerminals("6588000000610659")
    }

    private fun initSdkWrapper() {
        val apiKey = encryptedPrefs.getApiKey()
        val publicKey = encryptedPrefs.getPublicKey()
        ClearentWrapper.initializeSDK(
            applicationContext,
            Constants.BASE_URL_SANDBOX,
            publicKey,
            apiKey
        )
    }
}