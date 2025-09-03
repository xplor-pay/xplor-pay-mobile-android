package com.xplore.paymobile.data.web

import com.google.gson.annotations.SerializedName

data class LoggedOut(
    @SerializedName("loggedOut") val loggedOut: Boolean,
)
