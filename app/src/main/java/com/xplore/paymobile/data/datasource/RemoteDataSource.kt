package com.xplore.paymobile.data.datasource

import com.xplore.paymobile.data.remote.XplorApi
import com.xplore.paymobile.data.remote.model.SearchMerchantOptions
import com.xplore.paymobile.exceptions.AuthTokenException

class RemoteDataSource(
    private val xplorApi: XplorApi,
    private val sharedPreferencesDataSource: SharedPreferencesDataSource
) {

    private val authToken
        get() = sharedPreferencesDataSource.getAuthToken()?.bearerToken

    private fun getHeader() = authToken?.let { token ->
        mapOf(
            "Content-Type" to "application/json",
            "Accept" to "application/json, text/plain, */*",
            "Authorization" to token
        )
    } ?: throw AuthTokenException("Missing authToken from shared preferences. AuthToken is null.")

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