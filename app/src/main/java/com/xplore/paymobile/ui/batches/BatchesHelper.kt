package com.xplore.paymobile.ui.batches

import com.xplore.paymobile.data.datasource.NetworkResource
import com.xplore.paymobile.data.datasource.RemoteDataSource
import com.xplore.paymobile.data.remote.model.Batch
import com.xplore.paymobile.data.remote.model.OpenBatchResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class BatchesHelper @Inject constructor(private val remoteDataSource: RemoteDataSource) {

    private val bgScope = CoroutineScope(Dispatchers.IO)
    private var isLoading = false

    private val _resultsFlow = MutableStateFlow<List<Batch>>(listOf())
    val resultsFlow: Flow<List<Batch>> = _resultsFlow

    fun getOpenBatch() {
        getBatch()
    }

    // todo will the batches tab ever access closed transactions?
    private fun getBatch(batchStatus: String = "open") {
        bgScope.launch {
            isLoading = true
            when (
                val batchResource =
                    remoteDataSource.getBatches(
                        batchStatus,
                    )
            ) {
                is NetworkResource.Success -> {
                    val batchesList = batchResource.data as OpenBatchResponse
                    val batches = batchesList.payload?.batches?.batch
                    if (batches != null) {
                        _resultsFlow.emit(batches)
                    }
                    isLoading = false
                }
                is NetworkResource.Error -> {
                    _resultsFlow.emit(emptyList())
                    Timber.d("Batches request failed")
                    isLoading = false
                }
            }
        }
    }

    fun getLoading() = isLoading
}
