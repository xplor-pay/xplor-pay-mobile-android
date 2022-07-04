package com.xplore.paymobile.util

import android.content.Context
import androidx.core.content.edit

class SharedPreferencesDataSource(context: Context) {

    companion object {
        private const val KEY_PREFERENCES = "com.xplore.paymobile.READER_PREFERENCES"
        private const val FIRST_PAIR = "FIRST_PAIR_KEY"
    }

    private val sharedPrefs = context.getSharedPreferences(KEY_PREFERENCES, Context.MODE_PRIVATE)

    fun setFirstPair(firstPair: FirstPair) =
        sharedPrefs.edit { putInt(FIRST_PAIR, firstPair.ordinal) }

    fun getFirstPair(): FirstPair = FirstPair.fromOrdinal(retrieveFirstPair())

    private fun retrieveFirstPair(): Int =
        sharedPrefs.getInt(FIRST_PAIR, FirstPair.NOT_DONE.ordinal)

    enum class FirstPair {
        NOT_DONE, DONE;

        companion object {
            fun fromOrdinal(firstPairOrdinal: Int) = values().find { it.ordinal == firstPairOrdinal } ?: NOT_DONE
        }
    }
}