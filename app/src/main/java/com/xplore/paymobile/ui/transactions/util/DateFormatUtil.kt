package com.xplore.paymobile.ui.transactions.util

import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

object DateFormatUtil {

    fun formatDateTime(transactionDate: String, terminalTimezone: String?): String? {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            val formatter = SimpleDateFormat("MMM dd yyyy hh:mm a (z)", Locale.US)
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = dateFormat.parse(transactionDate)
            formatter.timeZone = TimeZone.getTimeZone(terminalTimezone)
            val formattedDate = date?.let { formatter.format(it) }
            formattedDate
        } catch (e: Exception) {
            Timber.d("Exception thrown while parsing transaction date")
            transactionDate
        }
    }
}