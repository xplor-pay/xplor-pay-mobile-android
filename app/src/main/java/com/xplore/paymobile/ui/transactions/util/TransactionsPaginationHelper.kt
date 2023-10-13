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

    private val _resultsFlow = MutableStateFlow<List<Transaction>>(listOf())
    val resultsFlow: Flow<List<Transaction>> = _resultsFlow

    var currentPage = 0


    fun nextPage() {
        //todo implement if statement
//        if (currentPage !isLoading) {
            requestTransactions()
//        }
        currentPage++
    }

    private fun requestTransactions() {
        bgScope.launch {
            when (val transactionResource =
                remoteDataSource.getTransactions(
                    currentPage.toString(),
                    PAGE_SIZE
                    )
                ) {
                is NetworkResource.Success -> {
                    val transactionList = transactionResource.data as TransactionResponse
                    val transactions = transactionList.payload.transactions?.transaction
                    if (transactions != null) {
                        _resultsFlow.emit(transactions)
                    }
                }
                is NetworkResource.Error -> {
                    _resultsFlow.emit(emptyList())
                    Timber.d("Transactions request failed")
                }
            }
        }
    }

    fun processTransaction(transactionItem: TransactionListAdapter.TransactionItem) {
        bgScope.launch {
            when (val transactionResource =
                remoteDataSource.processTransaction(
                    transactionItem
                )
            ) {
                is NetworkResource.Success -> {
                    val transactionList = transactionResource.data as TransactionResponse
                    val transactions = transactionList.payload.transactions?.transaction
                    if (transactions != null) {
                        _resultsFlow.emit(transactions)
                    }
                }
                is NetworkResource.Error -> {
                    Timber.d("Transaction request failed")
                }
            }
        }
    }

}