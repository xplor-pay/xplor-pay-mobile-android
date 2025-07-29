package com.xplore.paymobile.data.remote

import com.clearent.idtech.android.wrapper.http.model.TerminalSettingsResponse
import com.xplore.paymobile.data.remote.model.MerchantTerminalsResponse
import com.xplore.paymobile.data.remote.model.OpenBatchResponse
import com.xplore.paymobile.data.remote.model.TransactionResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.POST
import retrofit2.http.Query

interface ClearentGatewayApi {

    @GET("/rest/v2/batches")
    suspend fun getBatches(
        @HeaderMap headers: Map<String, String>,
        @Query("status") status: String,
    ): Response<OpenBatchResponse>

    @GET("/rest/v2/api/merchant/terminals?filter=mobile")
    suspend fun getMobileTerminals(
        @HeaderMap header: Map<String, String>,
    ): Response<MerchantTerminalsResponse>

    @GET("rest/v2/transactions")
    suspend fun getTransactions(
        @HeaderMap header: Map<String, String>,
        @Query("page") page: String,
        @Query("pageSize") pageSize: String,
    ): Response<TransactionResponse>

    @POST("rest/v2/transactions")
    suspend fun postTransaction(
        @HeaderMap header: Map<String, String>,
        @Body body: Map<String, String>,
    ): Response<TransactionResponse>

    @GET("rest/v2/settings/terminal")
    suspend fun getTerminalSettings(
        @HeaderMap header: Map<String, String>,
    ): Response<TerminalSettingsResponse>
}
