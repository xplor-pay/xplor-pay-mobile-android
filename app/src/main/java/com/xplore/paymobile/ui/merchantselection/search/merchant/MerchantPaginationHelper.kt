package com.xplore.paymobile.ui.merchantselection.search.merchant

import com.xplore.paymobile.data.datasource.NetworkResource
import com.xplore.paymobile.data.datasource.RemoteDataSource
import com.xplore.paymobile.data.remote.model.MerchantsResponse
import com.xplore.paymobile.data.remote.model.SearchMerchantOptions
import com.xplore.paymobile.data.web.Merchant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class MerchantPaginationHelper @Inject constructor(private val remoteDataSource: RemoteDataSource) {
    companion object {
        private const val PAGE_SIZE = 10
    }

    private val _resultsFlow = MutableStateFlow<List<Merchant>>(listOf())
    val resultsFlow: Flow<List<Merchant>> = _resultsFlow

    private val bgScope = CoroutineScope(Dispatchers.IO)
    var query = ""
        private set

    var currentPage = 1

    fun updateQuery(newQuery: String) {
        currentPage = 1
        query = newQuery
        requestMerchants()
    }

    fun nextPage() {
        currentPage++
        requestMerchants()
    }

    private fun requestMerchants() {
        bgScope.launch {
            when (val merchantsResource =
                remoteDataSource.searchMerchants(
                    getDefaultSearchOptionsFor(
                        query,
                        currentPage.toString()
                    )
                )) {
                is NetworkResource.Success -> {
                    val merchants = merchantsResource.data as MerchantsResponse
                    _resultsFlow.emit(merchants.content)
                }
                is NetworkResource.Error -> {
                    Timber.d("Merchants request failed")
                }
            }
        }
    }

    fun getDefaultSearchOptionsFor(query: String, pageNumber: String) =
        SearchMerchantOptions(
            searchString = query,
            pageNumber = pageNumber,
            pageSize = PAGE_SIZE.toString(),
            includeClosed = "false"
        )
}