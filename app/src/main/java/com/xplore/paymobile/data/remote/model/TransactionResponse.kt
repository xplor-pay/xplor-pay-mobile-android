package com.clearent.idtech.android.wrapper.http.model

import com.google.gson.annotations.SerializedName

data class TransactionResponse(
    @SerializedName("code") var code: String,
    @SerializedName("status") var status: String,
    @SerializedName("exchange-id") var exchangeId: String,
    var links: List<Links>,
    var payload: TransactionPayload
)

data class Links(
    @SerializedName("rel") var rel: String,
    @SerializedName("href") var href: String,
    @SerializedName("id") var id: String
)

data class TransactionPayload(
    var error: ResponseError,
    var transaction: Transaction?,
    @SerializedName("payloadType") var payloadType: String
)

data class ResponseError(
    @SerializedName("error-message") var errorMessage: String,
    @SerializedName("result-code") var resultCode: String? = null,
    @SerializedName("time-stamp") var timeStamp: String? = null
)

data class Transaction(
    @SerializedName("id") var id: String,
    @SerializedName("created") var created: String,
    @SerializedName("result") var result: String,
    @SerializedName("card") var card: String,
    @SerializedName("display-message") var displayMessage: String,
    @SerializedName("service-fee") var serviceFee: String?,
    @SerializedName("surcharge-applied") var surchargeApplied: Boolean?,
    @SerializedName("amount") var amount: String,
    @SerializedName("tip-amount") var tipAmount: String?,
    @SerializedName("result-code") var resultCode: String,
    @SerializedName("exp-date") var expDate: String,
    @SerializedName("last-four") var lastFour: String,
    @SerializedName("merchant-id") var merchantId: String,
    @SerializedName("terminal-id") var terminalId: String
)
