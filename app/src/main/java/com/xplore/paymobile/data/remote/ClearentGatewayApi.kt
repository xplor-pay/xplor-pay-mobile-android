package com.xplore.paymobile.data.remote

import com.clearent.idtech.android.BuildConfig
import com.xplore.paymobile.data.remote.model.MerchantTerminalsResponse
import com.xplore.paymobile.data.remote.model.OpenBatchResponse
import com.xplore.paymobile.data.remote.model.TerminalSettingsResponse
import com.xplore.paymobile.data.remote.model.TransactionResponse
import retrofit2.Response
import retrofit2.http.*

interface ClearentGatewayApi {

    companion object {
        val BASE_URL =
//            if (BuildConfig.DEBUG) "https://gateway-qa.clearent.net" else
                "https://gateway.clearent.net"
    }

    @GET("/rest/v2/batches")
    suspend fun getOpenBatch(
        @HeaderMap headers: Map<String, String>,
        @QueryMap filters: Map<String, String>
    ): Response<OpenBatchResponse>

    @GET("/rest/v2/api/merchant/terminals?filter=mobile")
    suspend fun getMobileTerminals(
        @HeaderMap header: Map<String, String>
    ): Response<MerchantTerminalsResponse>

    @GET("rest/v2/transactions")
    suspend fun getTransactions(
        @HeaderMap header: Map<String, String>,
        @Query("page") page: String,
        @Query("pageSize") pageSize: String
    ): Response<TransactionResponse>

    @POST("rest/v2/transactions")
    suspend fun postTransaction(
        @HeaderMap header: Map<String, String>,
        @Body body: Map<String, String>
    ): Response<TransactionResponse>

    @GET("rest/v2/settings/terminal")
    suspend fun getTerminalSettings(
        @HeaderMap header: Map<String, String>
    ): Response<TerminalSettingsResponse>
}