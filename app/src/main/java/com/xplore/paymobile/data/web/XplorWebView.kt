// package com.xplore.paymobile.data.web
//
// import android.annotation.SuppressLint
// import android.content.Context
// import android.content.Intent
// import android.webkit.*
// import androidx.core.content.ContextCompat
// import com.xplore.paymobile.util.Constants
// import timber.log.Timber
// import java.net.HttpURLConnection
//
// @SuppressLint("SetJavaScriptEnabled")
// open class XplorWebView(
//    private val webView: WebView,
//    jsBridge: JSBridge,
//    context: Context,
//    onWebViewSetupDone: (() -> Unit)? = null,
//    onPageLoaded: (() -> Unit)? = null
// ) {
//
//    init {
//        webView.run {
//            webViewClient = object : WebViewClient() {
//
//                override fun shouldOverrideUrlLoading(
//                    view: WebView?,
//                    request: WebResourceRequest?
//                ): Boolean {
//                    // if we are detecting a logout event
//                    if (request?.url.toString().contains(Constants.SIGNOUT_WEB_PAGE_URL)) {
//                        jsBridge.logout()
//                        return false
//                    }
//
//                    // if we are in our domain continue inside the app
//                    if (request?.url?.host in Constants.HOST_NAMES) {
//                        return false
//                    }
//
//                    // if the user gesture is absent ignore the url
//                    if (request?.hasGesture() != true) return true
//
//                    // otherwise open a browser
//                    Intent(Intent.ACTION_VIEW, request.url).run {
//                        ContextCompat.startActivity(context, this, null)
//                    }
//
//                    return true
//                }
//
//                override fun onReceivedHttpError(
//                    view: WebView?,
//                    request: WebResourceRequest?,
//                    errorResponse: WebResourceResponse?
//                ) {
//                    super.onReceivedHttpError(view, request, errorResponse)
//
//                    val statusCode = errorResponse?.statusCode
//                    if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED || statusCode == HttpURLConnection.HTTP_FORBIDDEN) {
//                        jsBridge.logout()
//                    }
//                }
//            }
//
//            webChromeClient = object : WebChromeClient() {
//
//                override fun onProgressChanged(view: WebView?, newProgress: Int) {
//                    if (progress == 100) {
//                        onPageLoaded?.invoke()
//                    }
//                }
//            }
//
//            settings.apply {
//                userAgentString = "xplor_mobile"
//                domStorageEnabled = true
//                javaScriptEnabled = true
//
//                allowFileAccess = false
//                setGeolocationEnabled(false)
//                allowContentAccess = false
//            }
//            addJavascriptInterface(jsBridge, "Android")
//        }
//        onWebViewSetupDone?.invoke()
//    }
//
//    fun runJsCommand(command: XplorLoginWebView.XplorJsCommand) {
//        Timber.d("Sending command to web view: $command")
//        command.evaluateJs(webView)
//    }
// }
