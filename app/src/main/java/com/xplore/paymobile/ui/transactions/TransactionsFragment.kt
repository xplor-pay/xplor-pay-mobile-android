package com.xplore.paymobile.ui.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.clearent.idtech.android.wrapper.ui.util.MarginItemDecoration
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.xplore.paymobile.R
import com.xplore.paymobile.data.remote.model.Transaction
import com.xplore.paymobile.databinding.FragmentTransactionsBinding
import com.xplore.paymobile.ui.base.BaseFragment
import com.xplore.paymobile.ui.transactions.model.TransactionItem
import com.xplore.paymobile.ui.transactions.util.DateFormatUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class TransactionsFragment : BaseFragment() {

    private val viewModel by viewModels<TransactionsViewModel>()
    private var transactionLinearLayout: LinearLayoutManager? = null

    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!

    private val transactionItemsAdapter =
        TransactionListAdapter(onItemClicked = { transactionItem, _ ->
            if (transactionItem.status != "Declined" && transactionItem.status != "Error") {
                val processType = determineTransactionProcessType(
                    transactionItem.type,
                    transactionItem.settled,
                    transactionItem.pending
                )

                if (processType.isNotBlank()) {
                    showProcessTransactionDialog(transactionItem, processType)
                }
            }
        })

    private fun determineTransactionProcessType(
        transactionType: String,
        isSettled: Boolean,
        isPending: Boolean
    ): String {
        return if (isSettled && transactionType != "REFUND" && transactionType != "UNMATCHED REFUND" && transactionType != "AUTH") {
            "Refund"
        } else if (!isSettled || (transactionType == "AUTH" && isPending)) {
            "Void"
        } else {
            ""
        }
    }

    private val concatAdapter = ConcatAdapter(transactionItemsAdapter)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        transactionBinding.progressBar.isVisible = true
        setupTransactionsFlow()
        setupTitle()
        setupLoadingFlow()
        setupTransactionRecycler()
//        setOnScrollListener()
    }

//    private fun setOnScrollListener() {
//        binding.transactionList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//                if (dy > 0 && transactionItemsAdapter.getCurrentScrollPosition() == viewModel.listOfCollectedTransactionItems.size - 1) {
//                    // Scrolling down
//                    print("last on the list")
//                } else if (dy < 0 && transactionItemsAdapter.getCurrentScrollPosition() == 0) {
//                    // Scrolling up
//                    print("first on the list")
//                }
//            }
//        })
//    }

//todo check for no internet or no terminal available

    private fun setupTransactionsFlow() {
        lifecycleScope.launch {
            viewModel.resultsFlow.collect { transactionList ->
                Timber.d("Received transactions ${transactionList.size}")
                if (transactionItemsAdapter.currentList.isNotEmpty()) {
                    val getMoreTransactions = transactionItemsAdapter.getCurrentScrollPosition()
                    if (viewModel.listOfCollectedTransactionItems.size == getMoreTransactions - 1) {
                        viewModel.nextPage()
                    }
                }
                submitList(transactionList)
            }
        }
    }

    private fun setupLoadingFlow() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loadingFlow.collect { isLoading ->
                    showLoading(isLoading)
                    when (isLoading) {
                        true -> {
//                            binding.progressBar.isVisible = true
//                            if (itemsAdapter.getCurrentScrollPosition() >= viewModel.totalResults.size - 5) {
//                                viewModel.nextPage()
//                            }
                        }
                        false -> {
//                            binding.progressBar.isVisible = false
                            if (transactionItemsAdapter.getCurrentScrollPosition() >= viewModel.listOfCollectedTransactionItems.size - 5) {
                                viewModel.nextPage()
                            }
                        }
                    }
                }
            }
        }
    }

    //todo move this method into the process transaction class
    private fun showProcessTransactionDialog(
        transactionItem: TransactionItem,
        transactionType: String
    ) {
        val processTransactionDialog = BottomSheetDialog(requireContext())
        processTransactionDialog.setContentView(R.layout.process_transaction)

        transactionLinearLayout = LinearLayoutManager(requireContext())

        val cancelButton = processTransactionDialog.findViewById<Button>(R.id.cancel_button)
        val processTransactionButton =
            processTransactionDialog.findViewById<Button>(R.id.process_transaction_button)
        processTransactionButton?.text = transactionType

        processTransactionDialog.findViewById<TextView>(R.id.amount)?.text =
            buildString {
                append("$")
                append(transactionItem.amount)
            }
        processTransactionDialog.findViewById<TextView>(R.id.created)?.text =
            DateFormatUtil.formatDateTime(transactionItem.created)

        cancelButton?.setOnClickListener {
            processTransactionDialog.dismiss()
        }

        processTransactionButton?.setOnClickListener {
            processTransaction(transactionItem)
        }

        processTransactionDialog.setCanceledOnTouchOutside(true)
        processTransactionDialog.setCancelable(true)
        processTransactionDialog.show()
    }

    //todo move this method into the process transaction class
    private fun processTransaction(transactionItem: TransactionItem) {
        println("here is id $id")
        viewModel.processTransaction(transactionItem)
    }

    //todo implement the progress bar
    private fun showLoading(loading: Boolean) {
//        transactionBinding.progressBar.isVisible = loading
    }

    private fun setupTitle() {
        with(binding) {
            toolbarLayout.toolbarTitle.text = getString(R.string.transactions_title)
            toolbarLayout.backButton.setOnClickListener {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun setupTransactionRecycler() {
        binding.apply {
            transactionItemsList.adapter = concatAdapter
            transactionItemsList.layoutManager = LinearLayoutManager(requireContext())
            transactionItemsList.addItemDecoration(MarginItemDecoration(40, 10))
        }
    }

    private fun submitList(transactionItemList: List<Transaction>) {
        transactionItemsAdapter.submitList(transactionItemList.map {
            TransactionItem(
                it.id,
                it.amount,
                it.created,
                it.type,
                it.status,
                it.card,
                it.settled,
                false
            )
        }) {
            if (viewModel.currentPage() == 1 && transactionItemList.isNotEmpty()) {
                binding.transactionItemsList.scrollToPosition(0)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}