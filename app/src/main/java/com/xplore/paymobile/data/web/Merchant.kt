package com.xplore.paymobile.data.web

import com.google.gson.annotations.SerializedName

data class Merchant(
    @SerializedName("address") val address: Address,
    @SerializedName("merchantName") val merchantName: String,
    @SerializedName("merchantNumber") val merchantNumber: String,
    @SerializedName("merchantStatus") val merchantStatus: MerchantStatus
)

data class Address(
    @SerializedName("city") val city: String,
    @SerializedName("country") val country: String,
    @SerializedName("line1") val line1: String,
    @SerializedName("line2") val line2: String,
    @SerializedName("line3") val line3: String,
    @SerializedName("merchantNumber") val merchantNumber: String,
    @SerializedName("state") val state: String,
    @SerializedName("zip") val zip: Any?
)

data class MerchantStatus(
    @SerializedName("externalDisplayCategory") val externalDisplayCategory: String,
    @SerializedName("isBoarded") val isBoarded: Boolean,
    @SerializedName("isEligibleForProcessing") val isEligibleForProcessing: Boolean,
    @SerializedName("merchantLifeCycleStatusDesc") val merchantLifeCycleStatusDesc: String,
    @SerializedName("merchantLifeCycleStatusId") val merchantLifeCycleStatusId: Int,
    @SerializedName("merchantLifeCycleStatusName") val merchantLifeCycleStatusName: String,
    @SerializedName("merchantNumber") val merchantNumber: String,
    @SerializedName("reportedCreateDateTime") val reportedCreateDateTime: String
)