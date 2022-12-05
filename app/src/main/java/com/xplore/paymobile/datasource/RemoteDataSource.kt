package com.xplore.paymobile.datasource

import com.xplore.paymobile.api.XplorApi
import com.xplore.paymobile.api.model.SearchMerchantOptions

class RemoteDataSource(
    private val xplorApi: XplorApi
) {

    var authToken: String? = null

    fun getHeader() = mapOf(
        "Content-Type" to "application/json",
        "Accept" to "application/json, text/plain, */*",
        "Authorization" to authToken!!
    )

    fun getHeader(merchantId: String) = mapOf(
        *getHeader().toList().toTypedArray(),
        "merchantid" to merchantId
    )

    suspend fun searchMerchants(searchMerchantOptions: SearchMerchantOptions) {
        xplorApi.searchMerchants(getHeader(), searchMerchantOptions)
    }
}