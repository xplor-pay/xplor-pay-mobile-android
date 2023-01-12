package com.xplore.paymobile.util

import com.clearent.idtech.android.BuildConfig

object Constants {

    val HOST_NAMES = listOf(
        "my-qa.clearent.net",
        "pgqa.clearent.net",
        "my.clearent.net",
        "pg.clearent.net"
    )

    // Credentials for Sandbox environment
    const val BASE_URL_SANDBOX = "https://gateway-sb.clearent.net"

    // Credentials for production environment
    const val BASE_URL_PROD = "https://gateway-pr.clearent.net"

    const val CLIENT_SUPPORT_PHONE_NUMBER = "866.435.0666"
    const val TERMS_AND_CONDITIONS_LINK = "https://clearent.com/merchant-agreement/"

    val BASE_URL_WEB_PAGE =
        if (BuildConfig.DEBUG) "https://my-qa.clearent.net" else "https://my.clearent.net"
}