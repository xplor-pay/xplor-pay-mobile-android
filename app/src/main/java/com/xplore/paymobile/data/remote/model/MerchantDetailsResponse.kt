package com.xplore.paymobile.data.remote.model

import com.google.gson.annotations.SerializedName

data class MerchantDetailsResponse(
    @SerializedName("content") val content: List<MerchantDetails>,
    @SerializedName("errored") val errored: Boolean,
    @SerializedName("metadata") val metadata: Metadata,
)

data class MerchantDetails(
    @SerializedName("feature") val feature: String,
    @SerializedName("isAvailable") val isAvailable: Boolean,
    @SerializedName("rolesWithAccess") val rolesWithAccess: List<String>?,
    @SerializedName("settings") val settings: Settings?,
    @SerializedName("url") val url: Any?,
)

data class Metadata(
    @SerializedName("exchangeId") val exchangeId: String,
    @SerializedName("timestamp") val timestamp: String,
)

data class Settings(
    @SerializedName("exportMenuMappings") val exportMenuMappings: List<ExportMenuMapping>?,
    @SerializedName("feedbackSurveyUrl") val feedbackSurveyUrl: String?,
)

data class ExportMenuMapping(
    @SerializedName("extension") val extension: String,
    @SerializedName("name") val name: String,
    @SerializedName("type") val type: String,
)
