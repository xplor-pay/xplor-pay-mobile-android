package com.xplore.paymobile.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.xplore.paymobile.util.SharedPreferencesDataSource

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPrefs = SharedPreferencesDataSource(application.applicationContext)

    fun firstPairDone() = sharedPrefs.setFirstPairDone(true)

    fun isFirstPairDone() = sharedPrefs.getFirstPairDone()
}