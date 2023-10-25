package com.xplore.paymobile.ui.transactions.util

import com.xplore.paymobile.data.datasource.NetworkResource
import com.xplore.paymobile.data.datasource.RemoteDataSource
import com.xplore.paymobile.data.remote.model.Transaction
import com.xplore.paymobile.data.remote.model.TransactionResponse
import com.xplore.paymobile.ui.transactions.adapter.TransactionListAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class TransactionsPaginationHelper @Inject constructor(private val remoteDataSource: RemoteDataSource) {
    companion object {
        private const val PAGE_SIZE = "25"
    }

    private val bgScope = CoroutineScope(Dispatchers.IO)
    private var currentPage = 0
    private var isLastPage = false
    private var isLoading = false
    private var isProcessTransactionSuccessful = false

    private val _resultsFlow = MutableStateFlow<List<Transaction>>(listOf())
    val resultsFlow: Flow<List<Transaction>> = _resultsFlow


    fun nextPage() {
        println("last: $isLastPage loading: $isLoading")
        if (!isLastPage &&  !isLoading) {
            requestTransactions()
        }
        currentPage++
    }

    private fun requestTransactions() {
        bgScope.launch {
            isLoading = true
            when (val transactionResource =
                remoteDataSource.getTransactions(
                    currentPage.toString(),
                    PAGE_SIZE
                    )
                ) {
                is NetworkResource.Success -> {
                    val transactionList = transactionResource.data as TransactionResponse
                    isLastPage = transactionList.page.last
                    val transactions = transactionList.payload.transactions?.transaction
                    if (transactions != null) {
                        _resultsFlow.emit(transactions)
                    }
                    isLoading = false
                }
                is NetworkResource.Error -> {
                    _resultsFlow.emit(emptyList())
                    Timber.d("Transactions request failed")
                    isLoading = false
                }
            }
        }
    }

    fun processTransaction(
        transactionItem: TransactionListAdapter.TransactionItem,
        transactionType: String
    ) {
        bgScope.launch {
            when (val transactionResource =
                remoteDataSource.processTransaction(
                    transactionItem.id,
                    transactionItem.amount,
                    transactionType
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
        currentPage = 0
        nextPage()
    }
}