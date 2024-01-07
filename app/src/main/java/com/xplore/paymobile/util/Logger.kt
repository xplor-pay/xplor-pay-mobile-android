package com.xplore.paymobile.util

import com.clearent.idtech.android.wrapper.ClearentWrapper
import com.xplore.paymobile.data.datasource.SharedPreferencesDataSource

object Logger {

    private val clearentWrapper = ClearentWrapper.getInstance()

    val sharedPrefs: SharedPreferencesDataSource? = null

    fun logMobileMessage(className: String, message: String) {
        if (message.isNotBlank()) {
            val logMessage = "$className: $message"
            sendLogsToClearentWrapper(logMessage)
        }
    }

    private fun sendLogsToClearentWrapper(logMessage: String) {
        println("logMessage: $logMessage")
        clearentWrapper.addRemoteLogRequest(Constants.APPLICATION_VERSION, logMessage)
    }
}