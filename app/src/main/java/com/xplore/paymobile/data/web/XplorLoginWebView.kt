package com.xplore.paymobile.data.web

import android.content.Context
import android.webkit.WebView
import com.xplore.paymobile.data.remote.model.Terminal
import timber.log.Timber


class XplorLoginWebView(
    private val webView: WebView,
    jsBridge: JSBridge,
    context: Context,
    onWebViewSetupDone: (() -> Unit)? = null
) : XplorWebView(webView, jsBridge, context, onWebViewSetupDone) {

    companion object {
        private const val changeMerchantJsCommand =
            "window.triggerExternalEvent('merchantSelected', { merchantName: \"%s\", merchantNumber:\"%s\" });"
        private const val changeTerminalJsCommand =
            "window.triggerExternalEvent('terminalSelected', %s);"
    }

    fun runJsCommand(command: XplorJsCommand) {
        Timber.d("Sending command to web view: $command")
        when (command) {
            is XplorJsCommand.ChangeMerchant -> webView.evaluateJavascript(
                changeMerchantJsCommand.format(
                    command.merchant.merchantName,
                    command.merchant.merchantNumber
                )
            ) {
                Timber.d("changeMerchantJsCommand callback value: $it")
            }
            is XplorJsCommand.ChangeTerminal -> webView.evaluateJavascript(
                changeTerminalJsCommand.format(command.terminal.terminalPKId)
            ) {
                Timber.d("changeTerminalJsCommand callback value: $it")
            }
        }
    }

    sealed class XplorJsCommand {

        data class ChangeMerchant(val merchant: Merchant) : XplorJsCommand()
        data class ChangeTerminal(val terminal: Terminal) : XplorJsCommand()
    }
}