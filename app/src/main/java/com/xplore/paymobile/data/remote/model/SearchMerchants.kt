package com.xplore.paymobile.data.remote.model

import com.google.gson.annotations.SerializedName

data class SearchMerchantOptions(
    @SerializedName("merchantNumberOrDbaSearchTerm") val searchString: String?,
    @SerializedName("pageNumber") val pageNumber: String,
    @SerializedName("pageSize") val pageSize: String,
    @SerializedName("includeClosed") val includeClosed: String = "false"
)