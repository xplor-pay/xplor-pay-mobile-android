package com.xplore.paymobile.ui.merchantselection.search.merchant

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MerchantSearchViewModel : ViewModel() {

    //TODO use correct object
    private val _resultsFlow = MutableStateFlow<List<String>>(listOf())
    val resultsFlow: StateFlow<List<String>> = _resultsFlow

    fun searchForQuery(query: String) {

    }
}