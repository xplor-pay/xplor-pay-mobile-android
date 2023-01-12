package com.xplore.paymobile.ui.transactions

import android.content.Context
import android.webkit.WebView
import androidx.lifecycle.ViewModel
import com.xplore.paymobile.data.datasource.NetworkResource
import com.xplore.paymobile.data.datasource.RemoteDataSource
import com.xplore.paymobile.data.web.JSBridge
import com.xplore.paymobile.data.web.setupWebView
import com.xplore.paymobile.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val jsBridge: JSBridge,
    private val remoteDataSource: RemoteDataSource
) : ViewModel() {

    companion object {
        private val transactionsPageBaseUrl =
            "${Constants.BASE_URL_WEB_PAGE}/ui/openbatchestransaction?BatchNumber=%s&StoreNumber=%s&StoreTerminalNumber=%s"
    }

    suspend fun prepareWebView(webView: WebView, context: Context) {
        val openBatch = remoteDataSource.getOpenBatch()

        if (openBatch !is NetworkResource.Success) return

        val batchNumber = openBatch.data?.payload?.batches?.batch?.get(0)?.id ?: return
        val terminalId = openBatch.data.payload?.batches?.batch?.get(0)?.terminalID ?: return
        val storeNumber = terminalId.take(4)
        val storeTerminalNumber = terminalId.drop(4)

        setupWebView(webView, context, jsBridge) {
            webView.loadUrl(
                transactionsPageBaseUrl.format(
                    batchNumber,
                    storeNumber,
                    storeTerminalNumber
                )
            )
        }
    }
}