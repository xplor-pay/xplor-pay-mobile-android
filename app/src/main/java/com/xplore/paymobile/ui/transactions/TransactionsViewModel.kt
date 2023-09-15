package com.xplore.paymobile.ui.transactions

import android.content.Context
import android.webkit.WebView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import com.clearent.idtech.android.wrapper.ClearentWrapper
import com.xplore.paymobile.data.datasource.NetworkResource
import com.xplore.paymobile.data.datasource.RemoteDataSource
import com.xplore.paymobile.data.datasource.SharedPreferencesDataSource
import com.xplore.paymobile.data.remote.model.Terminal
import com.xplore.paymobile.data.remote.model.Transaction
import com.xplore.paymobile.data.remote.model.TransactionResponse
import com.xplore.paymobile.data.web.JSBridge
import com.xplore.paymobile.data.web.Merchant
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val sharedPrefs: SharedPreferencesDataSource
//    val interactionDetector: UserInteractionDetector
) : ViewModel() {

    var transactions: List<Transaction> = emptyList()

    private val _resultsFlow = MutableStateFlow<List<Transaction>>(listOf())
    val resultsFlow: Flow<List<Transaction>> = _resultsFlow

    companion object {
//        private val transactionsPageBaseUrl =
//            "${Constants.BASE_URL_WEB_PAGE}/ui/openbatchestransaction?BatchNumber=%s&StoreNumber=%s&StoreTerminalNumber=%s"
    }

    private val clearentWrapper = ClearentWrapper.getInstance()

//    private lateinit var xplorWebView: XplorWebView

    val hasInternet
        get() = clearentWrapper.isInternetOn

    fun terminalAvailable() = sharedPrefs.getTerminal() != null

    suspend fun prepareWebView(context: Context) {
        val result = remoteDataSource.getTransactions()

        if (result !is NetworkResource.Success)
            return
        val transactionResponse: TransactionResponse? = result.data
        transactionResponse?.payload?.transactions
        println(result.data?.payload?.transactions)
        val transactions: List<Transaction> = result.data?.payload?.transactions?.transaction ?: emptyList()
        _resultsFlow.emit(transactions)
//        val batchNumber = transactions.data?.payload?.batches?.batch?.get(0)?.id ?: return
//        val terminalId = openBatch.data.payload?.batches?.batch?.get(0)?.terminalID ?: return
//        val storeNumber = terminalId.take(4)
//        val storeTerminalNumber = terminalId.drop(4)

//        xplorWebView = XplorWebView(webView, jsBridge, context,
//            onWebViewSetupDone = {
//                webView.loadUrl(
//                    transactionsPageBaseUrl.format(
//                        batchNumber,
//                        storeNumber,
//                        storeTerminalNumber
//                    )
//                )
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
    }

    fun currentPage(): Int = paginationHelper.currentPage

}

sealed class TransactionSelection {
    data class TransactionAvailable(val transaction: Transaction) : TransactionSelection()
    object NoTerminal : TransactionSelection()
}