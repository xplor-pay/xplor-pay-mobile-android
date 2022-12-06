package com.xplore.paymobile.data.datasource

import com.xplore.paymobile.data.remote.XplorApi
import com.xplore.paymobile.data.remote.model.SearchMerchantOptions
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteDataSource @Inject constructor(
    private val xplorApi: XplorApi
) {

    var authToken: String? = null

    private fun getHeader() = mapOf(
        "Content-Type" to "application/json",
        "Accept" to "application/json, text/plain, */*",
        "Authorization" to authToken!!
    )

    private fun getHeader(merchantId: String) = mapOf(
        *getHeader().toList().toTypedArray(),
        "MerchantId" to merchantId
    )

    suspend fun searchMerchants(searchMerchantOptions: SearchMerchantOptions) =
        xplorApi.searchMerchants(getHeader(), searchMerchantOptions)

    suspend fun getMerchantDetails(merchantId: String) =
        xplorApi.getMerchantDetails(
            getHeader(merchantId),
            merchantId
        )

    suspend fun fetchTerminals(merchantId: String) =
        xplorApi.fetchTerminals(getHeader(merchantId))
}