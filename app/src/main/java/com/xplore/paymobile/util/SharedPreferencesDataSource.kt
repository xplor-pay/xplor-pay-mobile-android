package com.xplore.paymobile.util

import android.content.Context
import androidx.core.content.edit

class SharedPreferencesDataSource(val context: Context) {

    companion object {
        private const val KEY_PREFERENCES = "com.xplore.paymobile.READER_PREFERENCES"
        private const val FIRST_PAIR_DONE = "FIRST_PAIR_DONE_KEY"
    }

    private val sharedPrefs = context.getSharedPreferences(KEY_PREFERENCES, Context.MODE_PRIVATE)

    fun setFirstPairDone(firstPairDone: Boolean) =
        sharedPrefs.edit { putBoolean(FIRST_PAIR_DONE, firstPairDone) }

    fun getFirstPairDone(): Boolean =
        sharedPrefs.getBoolean(FIRST_PAIR_DONE, false)
}