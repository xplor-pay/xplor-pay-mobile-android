package com.xplore.paymobile.util

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.xplore.paymobile.data.remote.model.Terminal
import com.xplore.paymobile.data.web.Merchant

class SharedPreferencesDataSource(private val context: Context, private val gson: Gson) {

    companion object {
        private const val KEY_PREFERENCES = "com.xplore.paymobile.READER_PREFERENCES"
        private const val FIRST_PAIR = "FIRST_PAIR_KEY"
        private const val MERCHANT = "MERCHANT_KEY"
        private const val TERMINALS = "TERMINALS_KEY"
        private const val SELECTED_TERMINAL = "SELECTED_TERMINAL_KEY"
    }

    private val sharedPrefs = context.getSharedPreferences(KEY_PREFERENCES, Context.MODE_PRIVATE)

    fun setFirstPair(firstPair: FirstPair) =
        sharedPrefs.edit { putInt(FIRST_PAIR, firstPair.ordinal) }

    fun getFirstPair(): FirstPair = FirstPair.fromOrdinal(retrieveFirstPair())

    private fun retrieveFirstPair(): Int =
        sharedPrefs.getInt(FIRST_PAIR, FirstPair.NOT_DONE.ordinal)

    fun setMerchant(merchant: Merchant) =
        sharedPrefs.edit { putString(MERCHANT, gson.toJson(merchant)) }

    fun getMerchant(): Merchant? {
        sharedPrefs.getString(MERCHANT, "").let { json ->
            if (json.isNullOrEmpty()) return null
            else return gson.fromJson(json, Merchant::class.java)
        }
    }

    fun setTerminal(terminal: Terminal) =
        sharedPrefs.edit { putString(SELECTED_TERMINAL, gson.toJson(terminal)) }

    fun clearTerminal() =
        sharedPrefs.edit { remove(SELECTED_TERMINAL) }

    fun getTerminal(): Terminal? {
        sharedPrefs.getString(SELECTED_TERMINAL, "").let { json ->
            if (json.isNullOrEmpty()) return null
            else return gson.fromJson(json, Terminal::class.java)
        }
    }

    enum class FirstPair {
        NOT_DONE, SKIPPED, DONE;

        companion object {
            fun fromOrdinal(firstPair: Int) = values().find { it.ordinal == firstPair } ?: NOT_DONE
        }
    }
}