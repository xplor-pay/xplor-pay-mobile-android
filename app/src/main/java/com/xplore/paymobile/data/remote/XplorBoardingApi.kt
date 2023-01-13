package com.xplore.paymobile.data.remote

import com.clearent.idtech.android.BuildConfig
import com.xplore.paymobile.data.remote.model.*
import retrofit2.Response
import retrofit2.http.*

interface XplorBoardingApi {

    companion object {
        val BASE_URL =
            if (BuildConfig.DEBUG) "https://boarding-qa.clearent.net" else "https://boarding.clearent.net"
    }

    @POST("/api/merchant-management/v1.0/search/Merchants")
    suspend fun searchMerchants(
        @HeaderMap headers: Map<String, String>,
        @Body body: SearchMerchantOptions
    ): Response<MerchantsResponse>
}