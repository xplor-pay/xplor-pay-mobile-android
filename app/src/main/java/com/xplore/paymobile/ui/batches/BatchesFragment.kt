package com.xplore.paymobile.ui.batches

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import com.xplore.paymobile.data.remote.model.Transaction
import com.xplore.paymobile.databinding.FragmentBatchesBinding
import com.xplore.paymobile.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class BatchesFragment : BaseFragment() {

    override val hasBottomNavigation: Boolean = true

    private val viewModel by viewModels<BatchesViewModel>()

    private var _binding: FragmentBatchesBinding? = null
    private val binding get() = _binding!!

//    private val transactionItemsAdapter =
//        TransactionListAdapter(onItemClicked = { transactionItem, _ ->
//            if (transactionItem.status != "Declined" && transactionItem.status != "Error") {
//                val processType = determineTransactionProcessType(
//                    transactionItem.type, transactionItem.settled, transactionItem.pending
//                )
//
//                if (processType.isNotBlank()) {
////                    showProcessTransactionDialog(transactionItem, processType)
//                }
//            }
//        })

    private fun determineTransactionProcessType(
        transactionType: String, isSettled: Boolean, isPending: Boolean
    ): String {
        return if (isSettled && transactionType != "REFUND" && transactionType != "UNMATCHED REFUND" && transactionType != "AUTH") {
            "Refund"
        } else if (!isSettled || (transactionType == "AUTH" && isPending)) {
            "Void"
        } else {
            ""
        }
    }

//    private val concatAdapter = ConcatAdapter(transactionItemsAdapter)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBatchesBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        binding.progressBar.isVisible = true
        setupTransactionsFlow()
//        setupTitle()
//        setupLoadingFlow()
//        setupTransactionList()
    }

//todo check for no internet or no terminal available?

    private fun setupTransactionsFlow() {
        lifecycleScope.launch {
            viewModel.resultsFlow.collect { transactions ->
                Timber.d("Received transactions ${transactions.size}")
//                if (transactionItemsAdapter.currentList.isNotEmpty()) {
//                val getMoreTransactions = transactionItemsAdapter.getCurrentScrollPosition()
//                    if (viewModel.listOfCollectedTransactions.size == getMoreTransactions - 1) {
//                        viewModel.nextPage()
//                    }
//                }
                submitList(transactions)
            }
        }
    }

//    private fun setupLoadingFlow() {
//        lifecycleScope.launch {
//            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                viewModel.loadingFlow.collect { isLoading ->
//                    showLoading(isLoading)
//                    when (isLoading) {
//                        true -> {
//                            binding.progressBar.isVisible = true
////                            if (itemsAdapter.getCurrentScrollPosition() >= viewModel.totalResults.size - 5) {
////                                viewModel.nextPage()
////                            }
//                        }
//                        false -> {
//                            if (transactionItemsAdapter.getCurrentScrollPosition() >= viewModel.listOfCollectedTransactions.size - 5) {
//                                viewModel.nextPage()
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }

//    private fun scrollListener() {
//        binding.transactionItemsList
//    }

    //todo move this method into the process transaction class
//    private fun showProcessTransactionDialog(
//        transactionItem: TransactionItem, transactionType: String
//    ) {
//        val processTransactionDialog = BottomSheetDialog(requireContext())
//        processTransactionDialog.setContentView(R.layout.process_transaction)
//
//        val cancelButton = processTransactionDialog.findViewById<Button>(R.id.cancel_button)
//        val processTransactionButton =
//            processTransactionDialog.findViewById<Button>(R.id.process_transaction_button)
//        processTransactionButton?.text = transactionType
//
//        processTransactionDialog.findViewById<TextView>(R.id.amount)?.text = buildString {
//            append("$")
//            append(transactionItem.amount)
//        }
//        processTransactionDialog.findViewById<TextView>(R.id.created)?.text =
//            DateFormatUtil.formatDateTime(transactionItem.created)
//
//        cancelButton?.setOnClickListener {
//            processTransactionDialog.dismiss()
//        }
//
//        processTransactionButton?.setOnClickListener {
//            processTransaction(transactionItem)
//        }
//
//        processTransactionDialog.setCanceledOnTouchOutside(true)
//        processTransactionDialog.setCancelable(true)
//        processTransactionDialog.show()
//    }
//
//    //todo move this method into the process transaction class
//    private fun processTransaction(transactionItem: TransactionItem) {
//        println("here is id $id")
//        viewModel.processTransaction(transactionItem)
//
//    }
//
//    //todo implement the progress bar
//    private fun showLoading(loading: Boolean) {
////        binding.progressBar.isVisible = loading
//    }
//
//    private fun setupTitle() {
//        with(binding) {
//            toolbarLayout.toolbarTitle.text = getString(R.string.transactions_title)
//            toolbarLayout.backButton.setOnClickListener {
//                requireActivity().onBackPressedDispatcher.onBackPressed()
//            }
//        }
//    }

//    private fun setupTransactionList() {
//        binding.apply {
//            transactionItemsList.adapter = concatAdapter
//            transactionItemsList.layoutManager = LinearLayoutManager(requireContext())
//            transactionItemsList.addItemDecoration(MarginItemDecoration(40, 10))
//        }
//    }

    private fun submitList(transactionList: List<Transaction>) {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}