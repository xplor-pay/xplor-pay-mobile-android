package com.xplore.paymobile

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
open class MainViewModel @Inject constructor() : ViewModel() {

    var loginVisible: Boolean = true
    var shouldShowForceLoginDialog = false
}