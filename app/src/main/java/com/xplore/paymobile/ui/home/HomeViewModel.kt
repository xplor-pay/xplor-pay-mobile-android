package com.xplore.paymobile.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.xplore.paymobile.util.SharedPreferencesDataSource
import com.xplore.paymobile.util.SharedPreferencesDataSource.*

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPrefs = SharedPreferencesDataSource(application.applicationContext)
    var isCardReaderSelected = true

    fun firstPairDone() = sharedPrefs.setFirstPair(FirstPair.DONE)
    fun firstPairSkipped() = sharedPrefs.setFirstPair(FirstPair.SKIPPED)

    fun getFirstPair() = sharedPrefs.getFirstPair()
    fun shouldShowHints() = getFirstPair() == FirstPair.NOT_DONE
}