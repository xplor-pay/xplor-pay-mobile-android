package com.xplore.paymobile.data.remote.model

import com.google.gson.annotations.SerializedName

data class OpenBatchResponse(
    @SerializedName("code") var code: String? = null,
    @SerializedName("status") var status: String? = null,
//    @SerializedName("exchange-id") var exchangeID: String? = null,
//    @SerializedName("links") var links: List<Links>? = null,
    @SerializedName("payload") var payload: Payload? = null,
)

data class Payload(
    @SerializedName("batches") var batches: Batches? = null,
)

data class Batches(
    @SerializedName("batch") var batch: List<Batch>? = null,
)

data class Batch(
    @SerializedName("id") var id: String? = null,
    @SerializedName("refund-count") var refundCount: String? = null,
    @SerializedName("refund-total") var refundTotal: String? = null,
    @SerializedName("sales-count") var salesCount: String? = null,
    @SerializedName("sales-total") var salesTotal: String? = null,
    @SerializedName("net-amount") var netAmount: String? = null,
    @SerializedName("status") var status: String? = null,
    @SerializedName("total-count") var totalCount: String? = null,
    @SerializedName("merchant-id") var merchantID: String? = null,
    @SerializedName("terminal-id") var terminalID: String? = null,
    @SerializedName("terminal-name") var terminalName: String? = null,
    @SerializedName("date-opened") var dateOpened: String? = null,
)
