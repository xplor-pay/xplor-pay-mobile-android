package com.xplore.paymobile.ui.settings

import androidx.lifecycle.ViewModel
import com.clearent.idtech.android.wrapper.ClearentWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val clearentWrapper = ClearentWrapper.getInstance()

    val hasInternet
        get() = clearentWrapper.isInternetOn
}
