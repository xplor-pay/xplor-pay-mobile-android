package com.xplore.paymobile.ui.batches

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.xplore.paymobile.R
import com.xplore.paymobile.data.remote.model.Batch
import com.xplore.paymobile.databinding.FragmentBatchesBinding
import com.xplore.paymobile.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BatchesFragment : BaseFragment() {

    override val hasBottomNavigation: Boolean = true

    private val viewModel by viewModels<BatchesViewModel>()

    private var _binding: FragmentBatchesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBatchesBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!viewModel.isLoading()) {
            setupTitle()
            setupLoadingFlow()
            loadBatch()
        }
    }

    private fun loadBatch() {
        if (viewModel.isLoading()) {
            return
        }
        binding.apply {
            viewModel.getBatches()
            setBatches()
        }
    }

    private fun setupTitle() {
        with(binding) {
            toolbarLayout.toolbarTitle.text = getString(R.string.batches_title)
            toolbarLayout.backButton.isVisible = false
        }
    }

    private fun setBatches() {
        if (viewModel.isLoading()) {
            return
        }
        lifecycleScope.launch {
            viewModel.resultsFlow.collect { batchList ->
                if (batchList.isNotEmpty())
                    setupViews(batchList)
            }
        }
    }

    private fun setupViews(batchList: List<Batch>) {
        with(binding) {
            val batch: Batch = batchList[0]
            batchNumber.text = buildString {
                append("#")
                append(batch.id)
            }
            batchTransactionTotal.text = buildString {
                append("Total Transactions: ")
                append(batch.totalCount)
            }
            batchSalesTotal.text = buildString {
                append("Sales (")
                append(batch.salesCount)
                append(")")
            }
            batchSalesTotalAmount.text = buildString {
                append("$")
                append(batch.salesTotal)
            }
            batchRefundTotal.text = buildString {
                append("Refunds (")
                append(batch.refundCount)
                append(")")
            }
            if(batch.refundTotal == "0.00" ) {
                batchRefundTotalAmount.text = buildString {
                    append("$")
                    append(batch.refundTotal)
                }
            } else {
                batchRefundTotalAmount.text = buildString {
                    append("-$")
                    append(batch.refundTotal)
                }
            }
            batchNetAmount.text = buildString {
                append("$")
                append(batch.netAmount)
            }

        }
    }

    private fun setupLoadingFlow() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loadingFlow.collect { isLoading ->
                    showLoading(isLoading)
                }
            }
        }
    }

    private fun showLoading(loading: Boolean) {
        binding.progressBar.isVisible = loading
    }

}