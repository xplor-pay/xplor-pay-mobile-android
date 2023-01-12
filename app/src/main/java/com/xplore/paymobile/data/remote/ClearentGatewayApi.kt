package com.xplore.paymobile.data.remote

import com.clearent.idtech.android.BuildConfig
import com.xplore.paymobile.data.remote.model.OpenBatchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.QueryMap

interface ClearentGatewayApi {

    companion object {
        val BASE_URL =
            if (BuildConfig.DEBUG) "https://gateway-qa.clearent.net" else "https://gateway.clearent.net"
    }

    @GET("/rest/v2/batches")
    suspend fun getOpenBatch(
        @HeaderMap headers: Map<String, String>,
        @QueryMap filters: Map<String, String>
    ): Response<OpenBatchResponse>
}