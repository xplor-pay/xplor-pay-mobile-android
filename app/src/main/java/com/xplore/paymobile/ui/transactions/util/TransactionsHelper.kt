package com.xplore.paymobile.ui.transactions.util

import com.xplore.paymobile.data.datasource.NetworkResource
import com.xplore.paymobile.data.datasource.RemoteDataSource
import com.xplore.paymobile.data.remote.model.Transaction
import com.xplore.paymobile.data.remote.model.TransactionResponse
import com.xplore.paymobile.ui.transactions.adapter.TransactionListAdapter
import com.xplore.paymobile.util.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class TransactionsHelper @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
) {
    companion object {
        private const val PAGE_SIZE = "30"
    }

    private val className: String = "TransactionHelper"

    private val bgScope = CoroutineScope(Dispatchers.IO)

    private var currentPage = 0
    private var isLastPage = false
    private var isLoading = false
    private var isProcessTransactionSuccessful = false

    private val _resultsFlow = MutableStateFlow<List<Transaction>>(listOf())
    val resultsFlow: Flow<List<Transaction>> = _resultsFlow

    fun nextPage() {
        if (!isLastPage && !isLoading) {
            requestTransactions()
        }
        currentPage++
    }

    private fun requestTransactions() {
        bgScope.launch {
            isLoading = true
            when (
                val transactionResource =
                    remoteDataSource.getTransactions(
                        currentPage.toString(),
                        PAGE_SIZE,
                    )
            ) {
                is NetworkResource.Success -> {
                    Logger.logMobileMessage(className, "Get transactions success")

                    val transactionList = transactionResource.data as TransactionResponse
                    isLastPage = transactionList.page.last
                    val transactions = transactionList.payload.transactions?.transaction
                    if (transactions != null) {
                        _resultsFlow.emit(transactions)
                    }
                    isLoading = false
                }
                is NetworkResource.Error -> {
                    Logger.logMobileMessage(className, "Get transactions failed")
                    _resultsFlow.emit(emptyList())
                    isLoading = false
                }
            }
        }
    }

    fun processTransaction(
        transactionItem: TransactionListAdapter.TransactionItem,
        transactionType: String,
    ) {
        bgScope.launch {
            when (
                remoteDataSource.processTransaction(
                    transactionItem.id,
                    transactionItem.amount,
                    transactionType,
                )
            ) {
                is NetworkResource.Success -> {
                    isProcessTransactionSuccessful = true
                    Timber.d("Transaction request successful")
                }
                is NetworkResource.Error -> {
                    isProcessTransactionSuccessful = false
                    Timber.d("Transaction request failed")
                }
            }
        }
    }

    fun isLoadingTransactions(): Boolean {
        return isLoading
    }

    fun getCurrentPage(): Int {
        return currentPage
    }

    fun isLastTransactionPage(): Boolean {
        return isLastPage
    }

    fun getProcessTransactionSuccessful(): Boolean {
        return isProcessTransactionSuccessful
    }

    fun refreshPage() {
        // todo: determine why collecting of transactions occurs twice.
        currentPage = 0
        nextPage()
    }
}
