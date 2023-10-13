package com.xplore.paymobile.data.remote.model

import com.google.gson.annotations.SerializedName

data class TransactionResponse(
    @SerializedName("code") var code: String,
    @SerializedName("status") var status: String,
    @SerializedName("exchange-id") var exchangeId: String,
    var page: PageData,
    var payload: TransactionPayload
)

data class TransactionPayload(
    var error: ResponseError,
    var transactions: Transactions?,
)

data class Transactions(
    @SerializedName("transaction") var transaction: ArrayList<Transaction>
)

data class ResponseError(
    @SerializedName("error-message") var errorMessage: String,
    @SerializedName("result-code") var resultCode: String? = null,
    @SerializedName("time-stamp") var timeStamp: String? = null
)

data class Transaction(
    var id: String,
    var created: String,
    var status: String,
    var card: String,
    var amount: String,
    var type: String,
    var settled: Boolean,
    var date: String
)

data class PageData(
    var last: Boolean
)