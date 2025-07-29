package com.xplore.paymobile.data.remote

import com.xplore.paymobile.data.remote.model.MerchantsResponse
import com.xplore.paymobile.data.remote.model.SearchMerchantOptions
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.POST

interface XplorBoardingApi {

    @POST("/api/merchant-management/v1.0/search/Merchants")
    suspend fun searchMerchants(
        @HeaderMap headers: Map<String, String>,
        @Body body: SearchMerchantOptions,
    ): Response<MerchantsResponse>
}
