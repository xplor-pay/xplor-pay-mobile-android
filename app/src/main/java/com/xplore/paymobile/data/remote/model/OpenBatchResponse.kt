package com.xplore.paymobile.data.remote.model

import com.google.gson.annotations.SerializedName


data class OpenBatchResponse(
    @SerializedName("code") val code: String,
    @SerializedName("exchange-id") val exchangeId: String,
    @SerializedName("links") val links: List<Link>,
    @SerializedName("page") val page: Page,
    @SerializedName("payload") val payload: Payload,
    @SerializedName("status") val status: String
)

data class Link(
    @SerializedName("href") val href: String, @SerializedName("rel") val rel: String
)

data class Page(
    @SerializedName("first") val first: Boolean,
    @SerializedName("last") val last: Boolean,
    @SerializedName("number") val number: Int,
    @SerializedName("number-of-elements") val numberOfElements: Int,
    @SerializedName("size") val size: Int,
    @SerializedName("sort") val sort: Sort,
    @SerializedName("total-elements") val totalElements: Int,
    @SerializedName("total-pages") val totalPages: Int
)

data class Payload(
    @SerializedName("batches") val batches: Batches,
    @SerializedName("payloadType") val payloadType: String
)

data class Sort(
    @SerializedName("field") val `field`: Field
)

data class Field(
    @SerializedName("batch-number") val batchNumber: String
)

data class Batches(
    @SerializedName("batch") val batch: List<Batch>
)

data class Batch(
    @SerializedName("date-opened") val dateOpened: String,
    @SerializedName("id") val id: String,
    @SerializedName("merchant-id") val merchantId: String,
    @SerializedName("net-amount") val netAmount: String,
    @SerializedName("refund-count") val refundCount: String,
    @SerializedName("refund-total") val refundTotal: String,
    @SerializedName("sales-count") val salesCount: String,
    @SerializedName("sales-total") val salesTotal: String,
    @SerializedName("status") val status: String,
    @SerializedName("terminal-id") val terminalId: String,
    @SerializedName("terminal-name") val terminalName: String,
    @SerializedName("total-count") val totalCount: String
)