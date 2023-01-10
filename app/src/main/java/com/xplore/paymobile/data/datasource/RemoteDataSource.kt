package com.xplore.paymobile.data.datasource

import com.xplore.paymobile.data.remote.ClearentGatewayApi
import com.xplore.paymobile.data.remote.XplorApi
import com.xplore.paymobile.data.remote.XplorBoardingApi
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

    private val authToken
        get() = sharedPreferencesDataSource.getAuthToken()?.bearerToken

    // TODO: Replace with quest jwt from terminal
    var foo = ""

    private fun getXplorApiHeader() = authToken?.let { token ->
        mapOf(
            "Content-Type" to "application/json",
            "Accept" to "application/json, text/plain, */*",
            "Authorization" to token
        )
    } ?: throw AuthTokenException("Missing authToken from shared preferences. AuthToken is null.")

    private fun getXplorApiHeader(merchantId: String) = mapOf(
        *getXplorApiHeader().toList().toTypedArray(), "MerchantId" to merchantId
    )

    private fun getClearentGatewayApiHeader() = mapOf(
        "Content-Type" to "application/json",
        "Accept" to "application/json, text/plain, */*",
        "Authorization" to "vt-token $foo"
    )

    private fun getOpenBatchFilters() = mapOf(
        "level" to "merchant", "status" to "OPEN"
    )

    suspend fun searchMerchants(searchMerchantOptions: SearchMerchantOptions) =
        try {
            val response = xplorBoardingApi.searchMerchants(getXplorApiHeader(), searchMerchantOptions)
            if (response.isSuccessful) {
                NetworkResource.Success(response.body())
            } else {
                NetworkResource.Error(errorBody = response.errorBody())
            }
        } catch (ex: Exception) {
            NetworkResource.Error(exception = ex)
        }

    suspend fun getMerchantDetails(merchantId: String) = xplorApi.getMerchantDetails(
        getXplorApiHeader(merchantId), merchantId
    )

    suspend fun fetchTerminals(merchantId: String) = try {
        val response = xplorApi.fetchTerminals(getXplorApiHeader(merchantId))
        if (response.isSuccessful) {
            NetworkResource.Success(response.body())
        } else {
            NetworkResource.Error(errorBody = response.errorBody())
        }
    } catch (ex: Exception) {
        NetworkResource.Error(exception = ex)
    }

    suspend fun getOpenBatch() = withContext(Dispatchers.IO) {
        try {
            val response = clearentGatewayApi.getOpenBatch(
                getClearentGatewayApiHeader(),
                getOpenBatchFilters()
            )

            return@withContext if (response.isSuccessful) {
                NetworkResource.Success(response.body())
            } else {
                NetworkResource.Error(errorBody = response.errorBody())
            }
        } catch (ex: Exception) {
            return@withContext NetworkResource.Error(exception = ex)
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