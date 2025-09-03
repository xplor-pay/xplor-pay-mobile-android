package com.xplore.paymobile.data.remote

import com.xplore.paymobile.data.remote.model.MerchantDetailsResponse
import com.xplore.paymobile.data.remote.model.TerminalsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Path

interface XplorApi {

    @GET("/api/merchantfrontendplatform/v1.0/features/web/{merchantId}")
    suspend fun getMerchantDetails(
        @HeaderMap headers: Map<String, String>,
        @Path("merchantId") merchantId: String,
    ): Response<MerchantDetailsResponse>

    // WARNING: The body will be null if there are no terminals for respective merchant
    @GET("/api/quest/terminals")
    suspend fun fetchTerminals(
        @HeaderMap headers: Map<String, String>,
    ): Response<TerminalsResponse>
}
