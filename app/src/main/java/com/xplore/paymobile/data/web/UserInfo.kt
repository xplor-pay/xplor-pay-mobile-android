package com.xplore.paymobile.data.web

import com.google.gson.annotations.SerializedName

data class UserInfo (
    @SerializedName("client_id") val clientId: String,
    @SerializedName("memberOf") var userRoles: List<String>,
    @SerializedName("principle") var userName: String
)
