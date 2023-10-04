package com.xplore.paymobile.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xplore.paymobile.data.remote.model.Transaction
import com.xplore.paymobile.ui.transactions.model.TransactionItem
import com.xplore.paymobile.ui.transactions.util.TransactionsPaginationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val paginationHelper: TransactionsPaginationHelper
) : ViewModel() {

    var listOfCollectedTransactionItems = mutableListOf<Transaction>()

    private val _resultsFlow = MutableStateFlow<List<Transaction>>(listOf())
    val resultsFlow: Flow<List<Transaction>> = _resultsFlow

    private val _loadingFlow = MutableStateFlow(false)
    val loadingFlow: StateFlow<Boolean> = _loadingFlow

    init {
        viewModelScope.launch {
            paginationHelper.resultsFlow.collect { transactions ->
                if (paginationHelper.currentPage >= 2) {
                    listOfCollectedTransactionItems.addAll(_resultsFlow.value)
                }
                listOfCollectedTransactionItems.addAll(transactions)
                _resultsFlow.emit(listOfCollectedTransactionItems)
            }
        }
    }

    fun processTransaction(transactionItem: TransactionItem) {
        paginationHelper.processTransaction(transactionItem)
    }

    fun nextPage() {
        paginationHelper.nextPage()
    }

    fun currentPage(): Int = paginationHelper.currentPage

}