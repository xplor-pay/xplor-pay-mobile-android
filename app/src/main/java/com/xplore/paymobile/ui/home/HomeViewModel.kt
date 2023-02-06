package com.xplore.paymobile.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.xplore.paymobile.data.datasource.SharedPreferencesDataSource
import com.xplore.paymobile.data.datasource.SharedPreferencesDataSource.FirstPair
import com.xplore.paymobile.data.remote.model.Terminal
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,
    private val sharedPrefs: SharedPreferencesDataSource
) : AndroidViewModel(application) {

    var isCardReaderSelected = true
    val terminalFlow = sharedPrefs.terminalFlow

    fun firstPairDone() = sharedPrefs.setFirstPair(FirstPair.DONE)
    fun firstPairSkipped() = sharedPrefs.setFirstPair(FirstPair.SKIPPED)

    fun getFirstPair() = sharedPrefs.getFirstPair()
    fun shouldShowHints() = getFirstPair() == FirstPair.NOT_DONE
}