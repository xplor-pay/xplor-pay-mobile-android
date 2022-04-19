package com.xplore.paymobile.util

fun String.insert(insertAt: Int, string: String): String {
    return this.substring(0, insertAt) + string + this.substring(insertAt, this.length)
}
