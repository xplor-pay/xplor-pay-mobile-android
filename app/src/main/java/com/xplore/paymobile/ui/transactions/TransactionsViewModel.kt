package com.xplore.paymobile.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xplore.paymobile.data.datasource.SharedPreferencesDataSource
import com.xplore.paymobile.data.remote.model.Transaction
import com.xplore.paymobile.ui.transactions.adapter.TransactionListAdapter
import com.xplore.paymobile.ui.transactions.util.TransactionsHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val paginationHelper: TransactionsHelper,
    private val sharedPrefs: SharedPreferencesDataSource,
) : ViewModel() {

    private val _resultsFlow = MutableStateFlow<List<Transaction>>(listOf())
    val resultsFlow: Flow<List<Transaction>> = _resultsFlow

    private val _loadingFlow = MutableStateFlow(false)
    val loadingFlow: StateFlow<Boolean> = _loadingFlow

    init {
        viewModelScope.launch {
            paginationHelper.resultsFlow.collect { transactions ->
                val listOfCollectedTransactionItems = mutableListOf<Transaction>()
                // todo test
//                val vtToken = clearentWrapper.sdkCredentials.clearentCredentials.toString()
                if (paginationHelper.getCurrentPage() >= 2) {
                    listOfCollectedTransactionItems.addAll(_resultsFlow.value)
                }
                listOfCollectedTransactionItems.addAll(transactions)
                _resultsFlow.emit(listOfCollectedTransactionItems)
            }
        }
    }

    fun processTransaction(
        transactionItem: TransactionListAdapter.TransactionItem,
        transactionType: String,
    ) {
        viewModelScope.launch {
            paginationHelper.processTransaction(transactionItem, transactionType)
        }
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

    fun getTerminalTimezone(): String? = sharedPrefs.getTerminalTimezone()

    fun isProcessTransactionSuccessful(): Boolean = paginationHelper.getProcessTransactionSuccessful()
    fun refreshPage() {
        paginationHelper.refreshPage()
    }

    fun hasVoidAndRefundPermissions() = sharedPrefs.canVoidOrRefundTransaction()

    fun getVtToken() = sharedPrefs.getTerminal()?.questJwt
}
