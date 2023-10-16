package com.xplore.paymobile.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xplore.paymobile.data.remote.model.Transaction
import com.xplore.paymobile.ui.transactions.adapter.TransactionListAdapter
import com.xplore.paymobile.ui.transactions.util.TransactionsPaginationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val paginationHelper: TransactionsPaginationHelper
) : ViewModel() {

    private val _resultsFlow = MutableStateFlow<List<Transaction>>(listOf())
    val resultsFlow: Flow<List<Transaction>> = _resultsFlow

    init {
        viewModelScope.launch {
            paginationHelper.resultsFlow.collect { transactions ->
                val listOfCollectedTransactionItems = mutableListOf<Transaction>()
                if (paginationHelper.getCurrentPage() >= 2) {
                    listOfCollectedTransactionItems.addAll(_resultsFlow.value)
                }
                listOfCollectedTransactionItems.addAll(transactions)
                _resultsFlow.emit(listOfCollectedTransactionItems)
            }
        }
    }

    fun processTransaction(transactionItem: TransactionListAdapter.TransactionItem) {
        paginationHelper.processTransaction(transactionItem)
    }

    fun nextPage() {
        paginationHelper.nextPage()
    }

    fun isLoading(): Boolean {
        return paginationHelper.isLoadingTransactions()
    }

    fun isLastTransactionPage(): Boolean {
        return paginationHelper.isLastTransactionPage()
    }

    fun currentPage(): Int = paginationHelper.getCurrentPage()

}