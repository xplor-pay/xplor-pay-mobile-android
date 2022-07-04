package com.xplore.paymobile.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.xplore.paymobile.util.SharedPreferencesDataSource
import com.xplore.paymobile.util.SharedPreferencesDataSource.*

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPrefs = SharedPreferencesDataSource(application.applicationContext)

    fun firstPairDone() = sharedPrefs.setFirstPair(FirstPair.DONE)

    fun getFirstPair() = sharedPrefs.getFirstPair()
    fun shouldShowHints() = getFirstPair() == FirstPair.NOT_DONE
}