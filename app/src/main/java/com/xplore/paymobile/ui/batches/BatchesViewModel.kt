package com.xplore.paymobile.ui.batches

import android.content.Context
import android.webkit.WebView
import androidx.lifecycle.ViewModel
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

    private lateinit var xplorWebView: XplorWebView

    fun prepareWebView(webView: WebView, context: Context, jsBridge: JSBridge) {
        xplorWebView = XplorWebView(webView, jsBridge, context) {
            webView.loadUrl(batchesPageUrl)
        }
    }
}