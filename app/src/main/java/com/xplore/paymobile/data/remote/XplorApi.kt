package com.xplore.paymobile.data.remote

import com.clearent.idtech.android.BuildConfig
import com.xplore.paymobile.data.remote.model.*
import retrofit2.Response
import retrofit2.http.*

interface XplorApi {

    companion object {
        val BASE_URL =
            if (BuildConfig.DEBUG) "https://api-qa.clearent.net" else "https://api.clearent.net"
    }

    @GET("/api/merchantfrontendplatform/v1.0/features/web/{merchantId}")
    suspend fun getMerchantDetails(
        @HeaderMap headers: Map<String, String>,
        @Path("merchantId") merchantId: String
    ): Response<MerchantDetailsResponse>

    // WARNING: The body will be null if there are no terminals for respective merchant
    @GET("/api/quest/terminals")
    suspend fun fetchTerminals(
        @HeaderMap headers: Map<String, String>
    ): Response<TerminalsResponse>
}