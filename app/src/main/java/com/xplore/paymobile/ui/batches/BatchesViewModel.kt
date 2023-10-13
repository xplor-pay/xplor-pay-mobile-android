package com.xplore.paymobile.ui.batches

import android.content.Context
import android.webkit.WebView
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import com.clearent.idtech.android.wrapper.ClearentWrapper
import com.xplore.paymobile.data.datasource.SharedPreferencesDataSource
import com.xplore.paymobile.data.remote.model.Transaction
import com.xplore.paymobile.data.web.JSBridge
import com.xplore.paymobile.interactiondetection.UserInteractionDetector
import com.xplore.paymobile.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class BatchesViewModel @Inject constructor(
    private val sharedPrefs: SharedPreferencesDataSource,
    val interactionDetector: UserInteractionDetector
) :
    ViewModel() {

    private val _resultsFlow = MutableStateFlow<List<Transaction>>(listOf())
    val resultsFlow: Flow<List<Transaction>> = _resultsFlow

    companion object {
        private val batchesPageUrl = "${Constants.BASE_URL_WEB_PAGE}/ui/openbatches"
    }

    private val clearentWrapper = ClearentWrapper.getInstance()

//    private lateinit var xplorWebView: XplorWebView

//    val hasInternet
//        get() = clearentWrapper.isInternetOn
//
//    fun prepareWebView(webView: WebView, context: Context, jsBridge: JSBridge) {
//        xplorWebView = XplorWebView(webView, jsBridge, context,
//            onWebViewSetupDone = {
//                webView.loadUrl(batchesPageUrl)
//            },
//            onPageLoaded = {
//                webView.isVisible = true
//                sharedPrefs.getMerchant()?.let {
//                    xplorWebView.runJsCommand(XplorLoginWebView.XplorJsCommand.ChangeMerchant(it))
//                }
//                sharedPrefs.getTerminal()?.let {
//                    xplorWebView.runJsCommand(XplorLoginWebView.XplorJsCommand.ChangeTerminal(it))
//                }
//            })
//    }
//
//    fun extendSession() {
//        xplorWebView.runJsCommand(XplorLoginWebView.XplorJsCommand.ExtendSession)
//    }
}