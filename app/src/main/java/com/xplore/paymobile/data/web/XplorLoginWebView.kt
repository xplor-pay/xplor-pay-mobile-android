package com.xplore.paymobile.data.web

import android.annotation.SuppressLint
import android.webkit.WebView
import com.xplore.paymobile.data.remote.model.Terminal

@SuppressLint("SetJavaScriptEnabled")
class XplorLoginWebView(
    private val webView: WebView,
    jsBridge: JSBridge,
    onWebViewSetupDone: (() -> Unit)? = null
) : XplorWebView(webView, jsBridge, onWebViewSetupDone) {

    companion object {
        private const val changeMerchantJsCommand =
            "window.triggerExternalEvent('merchantSelected', { merchantName: \\\"%s\\\", merchantNumber:\\\"%s\\\" });"
        private const val changeTerminalJsCommand =
            "window.triggerExternalEvent('terminalSelected', %s);"
    }

    fun runJsCommand(command: XplorJsCommand) {
        when (command) {
            is XplorJsCommand.ChangeMerchant -> webView.evaluateJavascript(
                changeMerchantJsCommand.format(
                    command.merchant.merchantName,
                    command.merchant.merchantNumber
                ),
                null
            )
            is XplorJsCommand.ChangeTerminal -> webView.evaluateJavascript(
                changeTerminalJsCommand.format(command.terminal.questJwt.terminalId),
                null
            )
        }
    }

    sealed class XplorJsCommand {

        data class ChangeMerchant(val merchant: Merchant) : XplorJsCommand()
        data class ChangeTerminal(val terminal: Terminal) : XplorJsCommand()
    }
}