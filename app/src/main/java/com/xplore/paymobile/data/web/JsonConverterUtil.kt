package com.xplore.paymobile.data.web

import com.google.gson.Gson
import com.xplore.paymobile.data.remote.model.Terminal
import javax.inject.Inject

class JsonConverterUtil @Inject constructor(private val gson: Gson) {

    fun jsonToAuthToken(json: String): AuthToken = gson.fromJson(json, AuthToken::class.java)
    fun jsonToMerchant(json: String): Merchant = gson.fromJson(json, Merchant::class.java)
    fun <T> toJson(item: T): String = gson.toJson(item)
    fun jsonToTerminal(json: String): Terminal = gson.fromJson(json, Terminal::class.java)
    fun jsonToLoggedOut(json: String): LoggedOut = gson.fromJson(json, LoggedOut::class.java)
    fun jsonToUserRoles(json: String): UserRoles = gson.fromJson(json, UserRoles::class.java)

    fun jsonToUserInfo(json: String): UserInfo = gson.fromJson(json, UserInfo::class.java)
}