package com.xplore.paymobile.data.web

import com.google.gson.annotations.SerializedName

data class ClientCredentials (
    @SerializedName("client_id") val clientId: String,
    @SerializedName("memberOf") val userRoles: List<String>
)
