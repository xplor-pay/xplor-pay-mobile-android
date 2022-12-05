package com.xplore.paymobile.api

import com.clearent.idtech.android.wrapper.http.model.SaleEntity
import com.clearent.idtech.android.wrapper.http.model.TransactionResponse
import com.xplore.paymobile.api.model.SearchMerchantOptions
import retrofit2.Response
import retrofit2.http.*

interface XplorApi {

    companion object {
        const val BASE_URL = "https://boarding-qa.clearent.net"
    }

    @POST("/api/merchant-management/v1.0/search/Merchant")
    suspend fun searchMerchants(
        @HeaderMap headers: Map<String, String>,
        @Body body: SearchMerchantOptions
    ): Response<TransactionResponse>

    @POST("/api/merchantfrontendplatform/v1.0/features/web/")
    suspend fun setMerchant(
        @HeaderMap headers: Map<String, String>,
        @QueryMap options: Map<String, String>,
        @Body body: SaleEntity
    ): Response<TransactionResponse>

    @POST("/api/merchant/terminals")
    suspend fun fetchTerminals(
        @HeaderMap headers: Map<String, String>,
        @QueryMap options: Map<String, String>,
        @Body body: SaleEntity
    ): Response<TransactionResponse>
}