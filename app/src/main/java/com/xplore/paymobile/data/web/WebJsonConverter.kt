package com.xplore.paymobile.data.web

import com.google.gson.Gson
import javax.inject.Inject

class WebJsonConverter @Inject constructor(private val gson: Gson) {

    fun jsonToAuthToken(json: String): AuthToken = gson.fromJson(json, AuthToken::class.java)
    fun jsonToMerchant(json: String): Merchant = gson.fromJson(json, Merchant::class.java)
    fun jsonToLoggedOut(json: String): LoggedOut = gson.fromJson(json, LoggedOut::class.java)
    fun jsonToUserRoles(json: String): UserRoles = gson.fromJson(json, UserRoles::class.java)
}