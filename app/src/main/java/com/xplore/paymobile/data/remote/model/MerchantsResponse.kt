package com.xplore.paymobile.data.remote.model

import com.google.gson.annotations.SerializedName
import com.xplore.paymobile.data.web.Merchant

data class MerchantsResponse(
    @SerializedName("content") val content: List<Merchant>,
)
