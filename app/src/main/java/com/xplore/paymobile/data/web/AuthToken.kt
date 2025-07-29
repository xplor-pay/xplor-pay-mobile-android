package com.xplore.paymobile.data.web

import com.google.gson.annotations.SerializedName

data class AuthToken(
    @SerializedName("bearerToken") val bearerToken: String,
)
