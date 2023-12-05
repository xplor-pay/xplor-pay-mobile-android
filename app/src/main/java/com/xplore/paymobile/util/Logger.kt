package com.xplore.paymobile.util

//import com.clearent.idtech.android.wrapper.ClearentWrapper
import com.clearent.idtech.android.wrapper.ClearentWrapper
import com.xplore.paymobile.data.datasource.SharedPreferencesDataSource

object Logger {

    private val clearentWrapper = ClearentWrapper.getInstance()

    private const val clientVersion: String = "ANDROID: Xplor Pay Mobile V1 - "

    val sharedPrefs: SharedPreferencesDataSource? = null

    fun logMobileMessage(className: String, message: String) {
        if (message.isNotBlank()) {
            val logMessage = "$className: $message"
            //todo let's not pass the vt token once we have everything in place for messaging
            if (sharedPrefs?.getTerminal()?.questJwt?.token != null)
                sendLogsToClearentWrapper(logMessage)
        }
//        val outboundMessage = "$clientVersion $message"
    }

    private fun sendLogsToClearentWrapper(logMessage: String) {
//        if (clearentWrapper.getCurrentTerminalSettings() == null) {
//        }
        println("logMessage: $logMessage")
//        clearentWrapper.logAppMessage(clientVersion, logMessage)
    }

//    companion object {
//        @Inject
//        lateinit var sharedPreferencesDataSource: SharedPreferencesDataSource
//
//        private val clearentWrapper = ClearentWrapper.getInstance()
//
//        private val clientVersion: String = "ANDROID: Xplor Pay Mobile V1 - "
//
//        fun logMessage(message: String) {
//            logMobileMessage(message)
//        }
//
//        private fun logMobileMessage(message: String) {
//
//            val terminal: Terminal? = sharedPreferencesDataSource.getTerminal()
//            if (terminal != null) {
//                val vtToken: String = terminal.questJwt.token
//                vtToken.let { clearentWrapper.logAppMessage(it, clientVersion, message) }
//            }
//        }
//    }
}