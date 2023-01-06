package com.xplore.paymobile.ui.batches

import com.xplore.paymobile.ActivityViewModel
import com.xplore.paymobile.data.web.JSBridge
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BatchesViewModel @Inject constructor(
    private val jsBridge: JSBridge
) : ActivityViewModel(jsBridge) {

}