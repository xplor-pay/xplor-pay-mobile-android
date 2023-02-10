package com.xplore.paymobile.ui.batches

import android.content.Context
import android.webkit.WebView
import androidx.lifecycle.ViewModel
import com.clearent.idtech.android.wrapper.ClearentWrapper
import com.xplore.paymobile.data.web.JSBridge
import com.xplore.paymobile.data.web.XplorWebView
import com.xplore.paymobile.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BatchesViewModel @Inject constructor() : ViewModel() {

    companion object {
        private val batchesPageUrl = "${Constants.BASE_URL_WEB_PAGE}/ui/openbatches"
    }

    private val clearentWrapper = ClearentWrapper.getInstance()

    private lateinit var xplorWebView: XplorWebView

    val hasInternet
        get() = clearentWrapper.isInternetOn

    fun prepareWebView(webView: WebView, context: Context, jsBridge: JSBridge) {
        xplorWebView = XplorWebView(webView, jsBridge, context) {
            webView.loadUrl(batchesPageUrl)
        }
    }
}