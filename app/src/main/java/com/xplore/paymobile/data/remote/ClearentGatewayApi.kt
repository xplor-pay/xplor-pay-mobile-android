package com.xplore.paymobile.data.remote

import com.xplore.paymobile.data.remote.model.OpenBatchResponse
import retrofit2.Response
import retrofit2.http.*

interface ClearentGatewayApi {

    companion object {
        const val BASE_URL = "https://gateway-qa.clearent.net"
    }

    @GET("/rest/v2/batches")
    suspend fun getOpenBatches(
        @HeaderMap headers: Map<String, String>,
        @QueryMap filters: Map<String, String>
    ): Response<OpenBatchResponse>
}