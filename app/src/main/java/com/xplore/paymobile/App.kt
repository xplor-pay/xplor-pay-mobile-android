package com.xplore.paymobile

import android.app.Application
import com.clearent.idtech.android.wrapper.ClearentWrapper
import com.clearent.idtech.android.wrapper.offline.config.OfflineModeConfig
import com.okta.authfoundation.AuthFoundationDefaults
import com.okta.authfoundation.client.OidcClient
import com.okta.authfoundation.client.OidcConfiguration
import com.okta.authfoundation.client.SharedPreferencesCache
import com.okta.authfoundation.credential.CredentialDataSource.Companion.createCredentialDataSource
import com.okta.authfoundationbootstrap.CredentialBootstrap
import com.xplore.paymobile.data.datasource.EncryptedSharedPrefsDataSource
import com.xplore.paymobile.data.datasource.SharedPreferencesDataSource
import com.xplore.paymobile.interactiondetection.AppLifecycleCallbacks
import com.xplore.paymobile.util.Constants
import dagger.hilt.android.HiltAndroidApp
import okhttp3.HttpUrl.Companion.toHttpUrl
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {

    private val clearentWrapper = ClearentWrapper.getInstance()

    @Inject
    lateinit var encryptedPrefs: EncryptedSharedPrefsDataSource

    @Inject
    lateinit var sharedPreferencesDataSource: SharedPreferencesDataSource

    @Inject
    lateinit var appLifecycleCallbacks: AppLifecycleCallbacks

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        AuthFoundationDefaults.cache = SharedPreferencesCache.create(this)
        val oidcConfiguration = OidcConfiguration(
            clientId = BuildConfig.CLIENT_ID,
            defaultScope = Constants.DEFAULT_SCOPE,
        )
        val client = OidcClient.createFromDiscoveryUrl(
            oidcConfiguration,
            BuildConfig.DISCOVERY_URL.toHttpUrl(),
        )
        CredentialBootstrap.initialize(client.createCredentialDataSource(this))

//        logoutWebView()

        encryptedPrefs = EncryptedSharedPrefsDataSource(applicationContext)
        registerActivityLifecycleCallbacks(appLifecycleCallbacks)
        generatePassphrase()
        initSdkWrapper()
    }

    // todo change the logout web view method name
//    private fun logoutWebView() {
//        sharedPreferencesDataSource.setAuthToken(null)
//    }

    private fun initSdkWrapper() {
        clearentWrapper.initializeSDK(
            context = applicationContext,
            baseUrl = BuildConfig.BASE_URL_GATEWAY,
            offlineModeConfig = OfflineModeConfig(encryptedPrefs.getDbPassphrase()),
        )
        ClearentWrapper.getInstance().addRemoteLogRequest(Constants.APPLICATION_VERSION, "Initialized SDK")
        // set up the sdk store and forward mode once so we don't override user preferences
        if (sharedPreferencesDataSource.isSdkSetUp()) return

        sharedPreferencesDataSource.sdkSetupComplete()
    }

    private fun generatePassphrase() {
        if (encryptedPrefs.getDbPassphrase().isEmpty()) {
            encryptedPrefs.setDbPassphrase(UUID.randomUUID().toString())
        }
    }
}
