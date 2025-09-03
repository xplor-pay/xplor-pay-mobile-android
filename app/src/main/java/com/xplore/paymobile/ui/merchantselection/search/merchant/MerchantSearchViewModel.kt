package com.xplore.paymobile.ui.merchantselection.search.merchant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clearent.idtech.android.wrapper.ClearentWrapper
import com.xplore.paymobile.data.datasource.SharedPreferencesDataSource
import com.xplore.paymobile.data.web.Merchant
import com.xplore.paymobile.ui.merchantselection.search.list.MerchantsListAdapter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MerchantSearchViewModel @Inject constructor(
    private val paginationHelper: MerchantPaginationHelper,
    private val sharedPrefs: SharedPreferencesDataSource,
) : ViewModel() {

    private val clearentWrapper = ClearentWrapper.getInstance()

    private val _resultsFlow = MutableStateFlow<List<Merchant>>(listOf())
    val resultsFlow: Flow<List<Merchant>> = _resultsFlow

    init {
        viewModelScope.launch {
            paginationHelper.resultsFlow.collect { merchants ->
                val totalResults = mutableListOf<Merchant>()
                if (paginationHelper.currentPage >= 2) {
                    totalResults.addAll(_resultsFlow.value)
                }
                totalResults.addAll(merchants)
                _resultsFlow.emit(totalResults)
            }
        }
    }

    fun searchForQuery(query: String) {
        viewModelScope.launch {
            Timber.d("Search merchants for $query")
            paginationHelper.updateQuery(query)
        }
    }

    fun nextPage() {
        paginationHelper.nextPage()
    }

    fun saveMerchant(merchantItem: MerchantsListAdapter.MerchantItem) {
        val merchant = _resultsFlow.value.find {
            it.merchantName == merchantItem.name && it.merchantNumber == merchantItem.id
        }
        merchant?.also {
            sharedPrefs.setMerchant(merchant)
            sharedPrefs.clearTerminal()

            clearentWrapper.sdkCredentials.clearentCredentials = null
        }
    }

    fun removeTerminal() {
        sharedPrefs.clearTerminal()
    }

    fun currentPage(): Int = paginationHelper.currentPage
}
