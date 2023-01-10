package com.xplore.paymobile.util

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable

fun String.insert(insertAt: Int, string: String): String {
    return this.substring(0, insertAt) + string + this.substring(insertAt, this.length)
}

inline fun <reified T : Parcelable> Intent?.parcelable(key: String): T? = when {
    this == null -> null
    Build.VERSION.SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
}

inline fun <reified T : Parcelable> Bundle?.parcelable(key: String): T? = when {
    this == null -> null
    Build.VERSION.SDK_INT >= 33 -> getParcelable(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelable(key) as? T
}
