package com.xplore.paymobile.data.remote.model

data class MerchantsResponse(
    val content: List<Merchant>
)

data class Merchant(
    val address: Address,
    val merchantName: String,
    val merchantNumber: String,
    val merchantStatus: MerchantStatus
)

data class Address(
    val city: String,
    val country: String,
    val line1: String,
    val line2: String,
    val line3: String,
    val merchantNumber: String,
    val state: String,
    val zip: Any
)

data class MerchantStatus(
    val externalDisplayCategory: String,
    val isBoarded: Boolean,
    val isEligibleForProcessing: Boolean,
    val merchantLifeCycleStatusDesc: String,
    val merchantLifeCycleStatusId: Int,
    val merchantLifeCycleStatusName: String,
    val merchantNumber: String,
    val reportedCreateDateTime: String
)