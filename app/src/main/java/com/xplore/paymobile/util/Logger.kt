package com.xplore.paymobile.util

import com.clearent.idtech.android.wrapper.ClearentWrapper
import com.xplore.paymobile.data.datasource.SharedPreferencesDataSource

object Logger {

    private val clearentWrapper = ClearentWrapper.getInstance()

    private const val clientVersion: String = "ANDROID: Xplor Pay Mobile V1 - "

    val sharedPrefs: SharedPreferencesDataSource? = null

    fun logMobileMessage(className: String, message: String) {
        if (message.isNotBlank()) {
            val logMessage = "$className: $message"
            sendLogsToClearentWrapper(logMessage)
        }
    }

    private fun sendLogsToClearentWrapper(logMessage: String) {
        //todo should probably check to see if the vt token is available before sending
        println("logMessage: $logMessage")
        clearentWrapper.logAppMessage(clientVersion, logMessage)
    }
}