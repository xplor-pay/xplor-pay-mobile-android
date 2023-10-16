package com.xplore.paymobile.ui.transactions.util

import com.xplore.paymobile.data.datasource.SharedPreferencesDataSource
import timber.log.Timber
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.*

object DateFormatUtil {

    fun formatDateTime(date: String, terminalTimezone: String): String? {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            val formatter = SimpleDateFormat("MMM dd yyyy, hh:mm a (z)", Locale.US)
            formatter.timeZone = TimeZone.getTimeZone(terminalTimezone)
            val formattedDate = parser.parse(date)?.let { formatter.format(it) }
            println("formatted date: $formattedDate")
            formattedDate
        } catch (e: Exception) {
            Timber.d("Exception thrown while parsing transaction date")
            ""
        }
    }
}