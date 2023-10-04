package com.xplore.paymobile.ui.transactions.list

import com.xplore.paymobile.data.datasource.NetworkResource
import com.xplore.paymobile.data.datasource.RemoteDataSource
import com.xplore.paymobile.data.remote.model.Transaction
import com.xplore.paymobile.data.remote.model.TransactionItem
import com.xplore.paymobile.data.remote.model.TransactionResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
//todo this class is more of a service than a helper class
class TransactionsPaginationHelper @Inject constructor(private val remoteDataSource: RemoteDataSource) {
    companion object {
        private const val PAGE_SIZE = "25"
    }

    private val _resultsFlow = MutableStateFlow<List<Transaction>>(listOf())
    val resultsFlow: Flow<List<Transaction>> = _resultsFlow

    private val bgScope = CoroutineScope(Dispatchers.IO)

    var currentPage = 0


    fun nextPage() {
        requestTransactions()
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
                    Timber.d("Transactions request failed")
                }
            }
        }
    }

    fun processTransaction(transactionItem: TransactionItem) {
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