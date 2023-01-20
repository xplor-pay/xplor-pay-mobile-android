package com.xplore.paymobile.ui.batches

import android.content.Context
import android.webkit.WebView
import androidx.lifecycle.ViewModel
import com.xplore.paymobile.data.datasource.SharedPreferencesDataSource
import com.xplore.paymobile.data.web.JSBridge
import com.xplore.paymobile.data.web.setupWebView
import com.xplore.paymobile.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BatchesViewModel @Inject constructor(
    private val sharedPrefs: SharedPreferencesDataSource
) : ViewModel() {

    companion object {
        private val batchesPageUrl = "${Constants.BASE_URL_WEB_PAGE}/ui/openbatches"
    }

    fun terminalAvailable() = sharedPrefs.getTerminal() != null

    fun prepareWebView(webView: WebView, context: Context, jsBridge: JSBridge) {
        setupWebView(webView, context, jsBridge) {
            webView.loadUrl(batchesPageUrl)
        }
    }
}