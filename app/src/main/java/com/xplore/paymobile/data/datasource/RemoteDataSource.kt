package com.xplore.paymobile.data.datasource

import com.xplore.paymobile.data.remote.ClearentGatewayApi
import com.xplore.paymobile.data.remote.XplorApi
import com.xplore.paymobile.data.remote.XplorBoardingApi
import com.xplore.paymobile.data.remote.model.OpenBatchResponse
import com.xplore.paymobile.data.remote.model.SearchMerchantOptions
import com.xplore.paymobile.exceptions.AuthTokenException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody

class RemoteDataSource(
    private val xplorApi: XplorApi,
    private val xplorBoardingApi: XplorBoardingApi,
    private val clearentGatewayApi: ClearentGatewayApi,
    private val sharedPreferencesDataSource: SharedPreferencesDataSource
) {

    private val coroutineContext = Dispatchers.IO

    private val authToken
        get() = sharedPreferencesDataSource.getAuthToken()?.bearerToken

    // TODO: replace with terminal vtToken from shared prefs
    var vtToken = ""

    private fun getHeader() = authToken?.let { token ->
        mapOf(
            "Content-Type" to "application/json",
            "Accept" to "application/json, text/plain, */*",
            "Authorization" to token
        )
    } ?: throw AuthTokenException("Missing authToken from shared preferences. AuthToken is null.")

    private fun getHeader(merchantId: String) = mapOf(
        *getHeader().toList().toTypedArray(), "MerchantId" to merchantId
    )

    private fun getClearentGatewayHeader() = mapOf(
        "Content-Type" to "application/json",
        "Accept" to "application/json, text/plain, */*",
        "authorization" to "vt-token $vtToken"
    )

    private fun getClearentGatewayQueries() = mapOf(
        "level" to "merchant", "status" to "OPEN"
    )

    suspend fun searchMerchants(searchMerchantOptions: SearchMerchantOptions) =
        xplorBoardingApi.searchMerchants(getHeader(), searchMerchantOptions)

    suspend fun getMerchantDetails(merchantId: String) = xplorApi.getMerchantDetails(
        getHeader(merchantId), merchantId
    )

    suspend fun fetchTerminals(merchantId: String) = xplorApi.fetchTerminals(getHeader(merchantId))

    suspend fun getOpenBatch(): NetworkResource<OpenBatchResponse?> =
        withContext(coroutineContext) {
            try {
                val response = clearentGatewayApi.getOpenBatches(
                    getClearentGatewayHeader(), getClearentGatewayQueries()
                )

                return@withContext if (response.isSuccessful) {
                    NetworkResource.Success(response.body())
                } else {
                    NetworkResource.Error(errorBody = response.errorBody())
                }
            } catch (e: Exception) {
                return@withContext NetworkResource.Error(exception = e)
            }
        }
}

sealed class NetworkResource<out T> {
    data class Success<T>(val data: T) : NetworkResource<T>()
    data class Error(
        val exception: Exception? = null,
        val errorBody: ResponseBody? = null
    ) : NetworkResource<Nothing>()
}