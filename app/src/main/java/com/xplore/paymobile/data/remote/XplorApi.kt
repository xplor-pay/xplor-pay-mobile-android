package com.xplore.paymobile.data.remote

import com.xplore.paymobile.data.remote.model.MerchantDetailsResponse
import com.xplore.paymobile.data.remote.model.MerchantsResponse
import com.xplore.paymobile.data.remote.model.SearchMerchantOptions
import com.xplore.paymobile.data.remote.model.TerminalsResponse
import retrofit2.Response
import retrofit2.http.*

interface XplorApi {

    companion object {
        const val BASE_URL_BOARDING = "https://boarding-qa.clearent.net"
        const val BASE_URL_API = "https://api-qa.clearent.net"
    }

    @POST("/api/merchant-management/v1.0/search/Merchants")
    suspend fun searchMerchants(
        @HeaderMap headers: Map<String, String>,
        @Body body: SearchMerchantOptions
    ): Response<MerchantsResponse>

    @GET("/api/merchantfrontendplatform/v1.0/features/web/{merchantId}")
    suspend fun getMerchantDetails(
        @HeaderMap headers: Map<String, String>,
        @Path("merchantId") merchantId: String
    ): Response<MerchantDetailsResponse>

    // The body will be null if there are no terminals for respective merchant
    @GET("$BASE_URL_API/api/quest/terminals")
    suspend fun fetchTerminals(
        @HeaderMap headers: Map<String, String>
    ): Response<TerminalsResponse>
}