package com.xplore.paymobile.data.datasource

import com.clearent.idtech.android.wrapper.http.model.TransactionType
import com.xplore.paymobile.data.remote.ClearentGatewayApi
import com.xplore.paymobile.data.remote.XplorApi
import com.xplore.paymobile.data.remote.XplorBoardingApi
import com.xplore.paymobile.data.remote.model.SearchMerchantOptions
import com.xplore.paymobile.exceptions.AuthTokenException
import com.xplore.paymobile.exceptions.VtTokenException
import com.xplore.paymobile.ui.transactions.adapter.TransactionListAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import timber.log.Timber

class RemoteDataSource(
    private val xplorApi: XplorApi,
    private val xplorBoardingApi: XplorBoardingApi,
    private val clearentGatewayApi: ClearentGatewayApi,
    private val sharedPreferencesDataSource: SharedPreferencesDataSource
) {

    private val authToken
        get() = sharedPreferencesDataSource.getAuthToken()

    private val vtToken
        get() = sharedPreferencesDataSource.getTerminal()?.questJwt?.token

    private fun getXplorApiHeader() = authToken?.let { token ->
        mapOf(
            "Content-Type" to "application/json",
            "Accept" to "application/json, text/plain, */*",
            "Authorization" to "Bearer $token"
        )
    } ?: throw AuthTokenException("Missing authToken from shared preferences. AuthToken is null.")

    private fun getXplorApiHeader(merchantId: String) = mapOf(
        *getXplorApiHeader().toList().toTypedArray(), "MerchantId" to merchantId
    )

    private fun getClearentGatewayApiHeader() = vtToken?.let { token ->
        mapOf(
            "Content-Type" to "application/json",
            "Accept" to "application/json, text/plain, */*",
            "Authorization" to "vt-token $token"
        )
    } ?: throw VtTokenException("Missing terminal from shared preferences. vt-token is null.")

//    private fun getCGWApiHeader() =
//        mapOf(
//            "Content-Type" to "application/json",
//            "Accept" to "application/json, text/plain, */*",
//            "api-key" to "1573dd1f92af43e6ae59a0e6e4f5f32f"
//        )


    private fun getClearentGatewayApiHeaderWithAuthToken(merchantId: String) = authToken?.let { token ->
        mapOf(
            "Content-Type" to "application/json",
            "Accept" to "application/json, text/plain, */*",
            "MerchantId" to merchantId,
            "Authorization" to "Bearer $token"
        )
    } ?: throw AuthTokenException("Missing authToken from shared preferences. AuthToken is null.")

    private fun getOpenBatchFilters() = mapOf(
        "level" to "merchant", "status" to "OPEN"
    )

    suspend fun searchMerchants(searchMerchantOptions: SearchMerchantOptions) = try {
        val response = xplorBoardingApi.searchMerchants(getXplorApiHeader(), searchMerchantOptions)
        if (response.isSuccessful) {
            NetworkResource.Success(response.body())
        } else {
            NetworkResource.Error(errorBody = response.errorBody())
        }
    } catch (ex: Exception) {
        NetworkResource.Error(exception = ex)
    }

    suspend fun getMerchantDetails(merchantId: String) = try {
        val response = xplorApi.getMerchantDetails(
            getXplorApiHeader(merchantId), merchantId
        )
        if (response.isSuccessful) {
            NetworkResource.Success(response.body())
        } else {
            NetworkResource.Error(errorBody = response.errorBody())
        }
    } catch (ex: Exception) {
        NetworkResource.Error(exception = ex)
    }

    suspend fun fetchTerminals(merchantId: String) = try {
        val response = xplorApi.fetchTerminals(getXplorApiHeader(merchantId))
        if (response.isSuccessful) {
            Timber.d("get terminals is success: ")
            NetworkResource.Success(response.body())
        } else {
            Timber.d("get terminals is not success: ")
            NetworkResource.Error(errorBody = response.errorBody())
        }
    } catch (ex: Exception) {
        Timber.d("exception_thrown: $ex")
        NetworkResource.Error(exception = ex)
    }

    suspend fun getMobileTerminals(merchantId: String) = try {
        val response = clearentGatewayApi.getMobileTerminals(getClearentGatewayApiHeaderWithAuthToken(merchantId))
        if (response.isSuccessful) {
            Timber.d("get mobile terminals is success: ")
            NetworkResource.Success(response.body())
        } else {
            Timber.d("get mobile terminals is not success: ")
            NetworkResource.Error(errorBody = response.errorBody())
        }
    } catch (ex: Exception) {
        NetworkResource.Error(exception = ex)
    }

    private var body = HashMap<String, String>()


//    suspend fun getOpenBatch() = withContext(Dispatchers.IO) {
//        try {
//            val response = clearentGatewayApi.getOpenBatch(
//                getClearentGatewayApiHeader(), getOpenBatchFilters()
//            )
//
//            return@withContext if (response.isSuccessful) {
//                NetworkResource.Success(response.body())
//            } else {
//                NetworkResource.Error(errorBody = response.errorBody())
//            }
//        } catch (ex: Exception) {
//            return@withContext NetworkResource.Error(exception = ex)
//        }
//    }

    suspend fun processTransaction(transactionItem: TransactionListAdapter.TransactionItem): NetworkResource<Any?> = withContext(Dispatchers.IO) {
        try {
            body["type"] = TransactionType.VOID.name
            body["id"] = transactionItem.id
            val response = clearentGatewayApi.postTransaction(getClearentGatewayApiHeader(), body)

            return@withContext if (response.isSuccessful) {
                NetworkResource.Success(response.body())
            } else {
                NetworkResource.Error(errorBody = response.errorBody())
            }
        } catch (ex: Exception) {
            return@withContext NetworkResource.Error(exception = ex)
        }
    }

    suspend fun getTransactions(page: String, pageSize: String): NetworkResource<Any?> = withContext(Dispatchers.IO) {
        try {
            val response = clearentGatewayApi.getTransactions(
                getClearentGatewayApiHeader(),
                page,
                pageSize
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

    suspend fun getTerminalSettings(): NetworkResource<Any?> = withContext(Dispatchers.IO) {
        try {
            val response = clearentGatewayApi.getTerminalSettings(
                getClearentGatewayApiHeader()
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