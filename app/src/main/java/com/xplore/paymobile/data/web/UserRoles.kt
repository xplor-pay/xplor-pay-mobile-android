package com.xplore.paymobile.data.web

import com.google.gson.annotations.SerializedName

data class UserRoles(
    @SerializedName("roles") val roles: List<String>
)