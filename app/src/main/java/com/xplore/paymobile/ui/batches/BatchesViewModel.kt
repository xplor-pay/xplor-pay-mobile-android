package com.xplore.paymobile.ui.batches

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xplore.paymobile.data.datasource.NetworkResource
import com.xplore.paymobile.data.datasource.RemoteDataSource
import com.xplore.paymobile.data.remote.model.Batch
import com.xplore.paymobile.data.remote.model.OpenBatchResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class BatchesViewModel @Inject constructor(
    private val batchesHelper: BatchesHelper
) :
    ViewModel() {

    private val _resultsFlow = MutableStateFlow<List<Batch>>(listOf())
    val resultsFlow: Flow<List<Batch>> = _resultsFlow

    private val _loadingFlow = MutableStateFlow(false)
    val loadingFlow: StateFlow<Boolean> = _loadingFlow

    init {
        viewModelScope.launch {
            batchesHelper.resultsFlow.collect { batchList ->
                _loadingFlow.emit(true)
                val batches = mutableListOf<Batch>()
                batches.addAll(batchList)
                if (batches.isNotEmpty()) {
                    _resultsFlow.emit(batches)
                }
                _loadingFlow.emit(false)
            }
        }
    }

    fun getBatches() {
        batchesHelper.getOpenBatch()
    }

    fun isLoading() = batchesHelper.getLoading()
}