package com.xplore.paymobile.ui.transactions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.xplore.paymobile.ActivityViewModel
import com.xplore.paymobile.data.web.JSBridge
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val jsBridge: JSBridge
) : ActivityViewModel(jsBridge) {

    private val _text = MutableLiveData<String>().apply {
        value = "This is transactions Fragment"
    }
    val text: LiveData<String> = _text
}