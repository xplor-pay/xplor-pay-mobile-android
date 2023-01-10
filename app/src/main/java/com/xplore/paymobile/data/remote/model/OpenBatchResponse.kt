package com.xplore.paymobile.data.remote.model

import com.google.gson.annotations.SerializedName

data class OpenBatchResponse(
    @SerializedName("code") var code: String? = null,
    @SerializedName("status") var status: String? = null,
    @SerializedName("exchange-id") var exchangeID: String? = null,
    @SerializedName("links") var links: List<Links>? = null,
    @SerializedName("payload") var payload: Payload? = null,
    @SerializedName("page") var page: Page? = null
)

data class Links(
    @SerializedName("rel") var rel: String? = null,
    @SerializedName("href") var href: String? = null
)

data class Page(
    @SerializedName("number") var number: String? = null,
    @SerializedName("total-pages") var totalPages: String? = null,
    @SerializedName("number-of-elements") var numberOfElements: String? = null,
    @SerializedName("total-elements") var totalElements: String? = null,
    @SerializedName("size") var size: String? = null,
    @SerializedName("sort") var sort: Sort? = null,
    @SerializedName("first") var first: String? = null,
    @SerializedName("last")  var last: String? = null
)

data class Sort(
    @SerializedName("field") var field: Field? = null
)

data class Field(
    @SerializedName("entry") var entry: Entry? = null
)

data class Entry(
    @SerializedName("key") var key: String? = null,
    @SerializedName("value") var value: String? = null
)

data class Payload(
    @SerializedName("batches") var batches: Batches? = null,
    @SerializedName("_type") var type: String? = null
)

data class Batches(
    @SerializedName("batch") var batch: List<Batch>? = null
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
    @SerializedName("date-opened") var dateOpened: String? = null
)
