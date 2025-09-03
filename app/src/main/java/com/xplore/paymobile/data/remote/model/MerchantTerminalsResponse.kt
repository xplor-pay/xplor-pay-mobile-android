package com.xplore.paymobile.data.remote.model

import com.google.gson.annotations.SerializedName

data class MerchantTerminalsResponse(
    @SerializedName("code") val code: String,
    @SerializedName("status") val status: String,
    @SerializedName("exchange-id") val exchangeId: String,
    @SerializedName("payload") val merchantTerminalsPayload: MerchantTerminalsPayload,
)

data class MerchantTerminalsPayload(
    @SerializedName("merchant-terminals") val merchantTerminals: List<MerchantTerminal>,
)

data class MerchantTerminal(
    @SerializedName("merchantTerminalId") val merchantTerminalId: String,
)
