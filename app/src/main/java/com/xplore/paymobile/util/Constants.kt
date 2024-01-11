package com.xplore.paymobile.util

import com.xplore.paymobile.BuildConfig

object Constants {

    val HOST_NAMES = listOf(
//        "my-qa.clearent.net",
        "auth-sb.clearent.net",
        "auth-qa.clearent.net",
        "auth-dev.clearent.net",
//        "my.clearent.net",
        "auth.clearent.net",
    )

    // Credentials for Sandbox environment
//    const val BASE_URL_SANDBOX = "https://gateway-sb.clearent.net"
    const val BASE_URL_SANDBOX = "https://gateway.clearent.net"

    // Credentials for production environment
//    const val BASE_URL_PROD = "https://gateway-qa.clearent.net"
    const val BASE_URL_PROD = "https://gateway.clearent.net"
//    const val BASE_URL_PROD = "https://auth.clearent.net/oauth2/aus4ulyubshD7M0yf697/.well-known/openid-configuration"

    const val CLIENT_SUPPORT_PHONE_NUMBER = "866.435.0666"
    const val TERMS_AND_CONDITIONS_LINK = "https://clearent.com/merchant-agreement/"

    val BASE_URL_WEB_PAGE =
//        if (BuildConfig.DEBUG) "https://my-qa.clearent.net" else
            "https://gateway.clearent.net"

    val SIGNOUT_WEB_PAGE_URL =
//        if (BuildConfig.DEBUG) "https://pgsb.clearent.net/_layouts/PG/signout.aspx" else
            "https://pg.clearent.net/_layouts/PG/signout.aspx"

    //okta credentials
    const val CLIENT_ID = "0oa6ggt30dFSxSVxX697"
    const val DISCOVERY_URL = "https://auth.clearent.net/oauth2/aus4ulyubshD7M0yf697/.well-known/openid-configuration"
    const val SIGN_IN_REDIRECT = "xplor.pay.mobile:/callback"
    const val LOGOUT_REDIRECT = "xplor.pay.mobile:/callback"

    const val SB_CLIENT_ID = "0oa3a1ic7mGSRLqrZ1d7"
    const val SB_DISCOVERY_URL = "https://auth-sb.clearent.net/oauth2/aus3a1kavt9qzEcsz1d7/.well-known/openid-configuration"

    const val DEFAULT_SCOPE = "openid profile offline_access"

    const val APPLICATION_VERSION: String = "ANDROID: Xplor Pay Mobile - 1.0.10"
}
