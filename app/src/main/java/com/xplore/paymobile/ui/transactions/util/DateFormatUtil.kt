package com.xplore.paymobile.ui.transactions.util

import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

object DateFormatUtil {

    fun formatDateTime(date: String): String? {
        return try {
            val modifiedDate = date.replace(" ", "T")
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            val formatter = SimpleDateFormat("MMM dd yyyy, hh:mm a", Locale.US)
            val formattedDate = parser.parse(modifiedDate)?.let { formatter.format(it) }
            println("formatted date: $formattedDate")
            //todo handle time zone. handle the code with
    //        return "$formattedDate (CDT)"
            formattedDate
        } catch (e: Exception) {
            Timber.d("Exception thrown while parsing transaction date")
            ""
        }
    }
}