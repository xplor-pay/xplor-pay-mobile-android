package com.xplore.paymobile.ui.batches

import android.content.Context
import android.webkit.WebView
import androidx.lifecycle.ViewModel
import com.xplore.paymobile.data.web.JSBridge
import com.xplore.paymobile.data.web.setupWebView
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BatchesViewModel @Inject constructor(
    private val jsBridge: JSBridge
) : ViewModel() {

    companion object {
        private const val openBatchesPageUrl = "https://my-qa.clearent.net/ui/openbatches"
    }

    fun prepareWebView(webView: WebView, context: Context) {
        setupWebView(webView, context, jsBridge) {
            webView.loadUrl(openBatchesPageUrl)
        }
    }
}