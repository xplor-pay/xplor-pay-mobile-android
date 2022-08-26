package com.xplore.paymobile

import android.app.Application
import com.clearent.idtech.android.wrapper.SDKWrapper
import com.xplore.paymobile.util.Constants
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        initSdkWrapper()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    private fun initSdkWrapper() = SDKWrapper.initializeReader(
            applicationContext,
            Constants.BASE_URL_SANDBOX,
            Constants.PUBLIC_KEY_SANDBOX,
            Constants.API_KEY_SANDBOX
        )
}