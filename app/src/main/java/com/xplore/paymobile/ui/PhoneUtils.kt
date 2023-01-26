package com.xplore.paymobile.ui

import android.content.Context
import android.content.Intent
import android.net.Uri

object PhoneUtils {

    fun dialNumber(context: Context, number: String) {
        val phoneIntent = Intent(Intent.ACTION_DIAL)
        phoneIntent.data = Uri.parse("tel:$number")
        context.startActivity(phoneIntent)
    }
}