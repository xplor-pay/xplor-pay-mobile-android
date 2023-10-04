package com.xplore.paymobile.data.remote.model

import com.clearent.idtech.android.wrapper.http.model.Links
import com.google.gson.annotations.SerializedName

// NOTE: use @Expose for any response classes using gson to json as the firstname/lastname/email.
//       use the password type (*********) in place of these fields. Expose annotation
//       will override this behavior
data class TransactionResponse(
    @SerializedName("code") var code: String,
    @SerializedName("status") var status: String,
    @SerializedName("exchange-id") var exchangeId: String,
    var links: List<Links>,
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
    var date: String,
    var pending: String
//    @SerializedName("display-message") var displayMessage: String,
//    @SerializedName("service-fee") var serviceFee: String?,
//    @SerializedName("surcharge-applied") var surchargeApplied: Boolean?,
//    @SerializedName("tip-amount") var tipAmount: String?,
//    @SerializedName("result-code") var resultCode: String,
//    @SerializedName("exp-date") var expDate: String,
//    @SerializedName("last-four") var lastFour: String,
//    @SerializedName("merchant-id") var merchantId: String,
//    @SerializedName("terminal-id") var terminalId: String,
)