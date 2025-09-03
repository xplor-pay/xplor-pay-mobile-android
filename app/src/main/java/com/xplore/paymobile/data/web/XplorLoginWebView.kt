// package com.xplore.paymobile.data.web
//
// import android.content.Context
// import android.webkit.WebView
// import com.xplore.paymobile.data.remote.model.Terminal
// import timber.log.Timber
//
//
// class XplorLoginWebView(
//    private val webView: WebView,
//    jsBridge: JSBridge,
//    context: Context,
//    onWebViewSetupDone: (() -> Unit)? = null,
//    onPageLoaded: (() -> Unit)? = null
// ) : XplorWebView(webView, jsBridge, context, onWebViewSetupDone, onPageLoaded) {
//
//    companion object {
//        private const val changeMerchantJsCommand =
//            "window.triggerExternalEvent('merchantSelected', { merchantName: \"%s\", merchantNumber:\"%s\" });"
//        private const val changeTerminalJsCommand =
//            "window.triggerExternalEvent('terminalSelected', %s);"
//        private const val extendSessionCommand =
//            "window.triggerExternalEvent('sessionExtended', true);"
//    }
//
//    sealed class XplorJsCommand(val js: String) {
//
//        abstract fun evaluateJs(webView: WebView)
//
//        data class ChangeMerchant(
//            val merchant: Merchant
//        ) : XplorJsCommand(changeMerchantJsCommand) {
//
//            override fun evaluateJs(webView: WebView) {
//                webView.evaluateJavascript(
//                    js.format(
//                        merchant.merchantName,
//                        merchant.merchantNumber
//                    )
//                ) {
//                    Timber.d("changeMerchantJsCommand callback value: $it")
//                }
//            }
//        }
//
//        data class ChangeTerminal(
//            val terminal: Terminal
//        ) : XplorJsCommand(changeTerminalJsCommand) {
//
//            override fun evaluateJs(webView: WebView) {
//                webView.evaluateJavascript(
//                    js.format(terminal.terminalPKId)
//                ) {
//                    Timber.d("changeTerminalJsCommand callback value: $it")
//                }
//            }
//        }
//
//        object ExtendSession : XplorJsCommand(extendSessionCommand) {
//            override fun evaluateJs(webView: WebView) {
//                webView.evaluateJavascript(
//                    extendSessionCommand
//                ) {
//                    Timber.d("extendSessionJsCommand callback value: $it")
//                }
//            }
//        }
//    }
// }
