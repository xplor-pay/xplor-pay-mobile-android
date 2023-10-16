package com.xplore.paymobile.ui.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.clearent.idtech.android.wrapper.ui.util.MarginItemDecoration
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.xplore.paymobile.R
import com.xplore.paymobile.data.remote.model.Transaction
import com.xplore.paymobile.databinding.FragmentTransactionsBinding
import com.xplore.paymobile.ui.base.BaseFragment
import com.xplore.paymobile.ui.transactions.adapter.TransactionListAdapter
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

    private val transactionListAdapter =
        TransactionListAdapter(onItemClicked = { transactionItem, _ ->
            if (transactionItem.status != "Declined" && transactionItem.status != "Error") {
                val processType = transactionItem.settled?.let {
                    determineTransactionProcessType(
                        transactionItem.type,
                        it,
                        transactionItem.pending
                    )
                }

                if (processType?.isNotBlank()!!) {
                    showProcessTransactionDialog(transactionItem, processType)
                }
            }
        })

    private fun determineTransactionProcessType(
        transactionType: String?,
        isSettled: Boolean,
        //todo fix this
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

    private val concatAdapter = ConcatAdapter(transactionListAdapter)

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
        loadTransactions()
        setupTitle()
        setOnScrollListener()
        setupTransactionList()
    }

    //not sure how to handle the dy.  seems to vary. the implementation seems misused but it works for
    private fun setOnScrollListener() {
        binding.transactionItemsList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (transactionListAdapter.getCurrentScrollPosition() == transactionListAdapter.currentList.size - 10) {
                    // Scrolling down
                    if (!viewModel.isLoading() && !viewModel.isLastTransactionPage()) {
                        viewModel.nextPage()
                    }
                }
//                println("shouldn't show dx: $dx  dy: $dy")
                //todo handle refresh here
//                else if (transactionListAdapter.getCurrentScrollPosition() == viewModel.listOfCollectedTransactionItems.size - 5 && !isLoading && viewModel.listOfCollectedTransactionItems.size != 25) {
                    // Scrolling up
//                    println("dx: $dx  dy: $dy")
//                    isLoading = true
//                    viewModel.nextPage()
//                    isLoading = false
//                }
            }
        })
    }

    private fun loadTransactions() {
        lifecycleScope.launch {
            viewModel.resultsFlow.collect { transactionList ->
                Timber.d("Received transactions ${transactionList.size}")
//                if (transactionListAdapter.currentList.isNotEmpty()) {
//                    transactionListAdapter..notifyItemChanged(0)
//                }
                submitList(transactionList)
            }
        }
    }

    //todo move this method into the process transaction class
    private fun showProcessTransactionDialog(
        transactionItem: TransactionListAdapter.TransactionItem,
        transactionType: String
    ) {
        val processTransactionDialog = BottomSheetDialog(requireContext())
        processTransactionDialog.setContentView(R.layout.process_transaction)

        transactionLinearLayout = LinearLayoutManager(requireContext())

        val cancelButton = processTransactionDialog.findViewById<Button>(R.id.cancel_button)
        val processTransactionButton =
            processTransactionDialog.findViewById<Button>(R.id.process_transaction_button)
        processTransactionButton?.text = transactionType

        processTransactionDialog.findViewById<TextView>(R.id.process_amount)?.text =
            buildString {
                append("$")
                append(transactionItem.amount)
            }
        processTransactionDialog.findViewById<TextView>(R.id.process_created)?.text =
            transactionItem.created?.let { DateFormatUtil.formatDateTime(it, viewModel.getTerminalTimezone()) }

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

    //todo move this method into the process transaction class (make adapter with onclick behavior)
    private fun processTransaction(transactionItem: TransactionListAdapter.TransactionItem) {
        println("here is id $id")
        viewModel.processTransaction(transactionItem)
    }

    private fun setupTitle() {
        with(binding) {
            toolbarLayout.toolbarTitle.text = getString(R.string.transactions_title)
            toolbarLayout.backButton.setOnClickListener {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun setupTransactionList() {
        binding.apply {
            transactionItemsList.adapter = concatAdapter
            transactionItemsList.layoutManager = LinearLayoutManager(requireContext())
            transactionItemsList.addItemDecoration(MarginItemDecoration(40, 10))
            viewModel.nextPage()
        }
    }

    private fun submitList(transactionItemList: List<Transaction>) {
        transactionListAdapter.submitList(transactionItemList.map {
            TransactionListAdapter.TransactionItem(
                it.id,
                it.amount,
                formatCreatedDate(it.created),
                it.type,
                it.status,
                it.card,
                it.settled,
                it.pending
            )
        }) {
            if (viewModel.currentPage() == 0 && transactionItemList.isNotEmpty()) {
                binding.transactionItemsList.scrollToPosition(0)
            }
        }
    }

    private fun formatCreatedDate(created: String): String? {
        return DateFormatUtil.formatDateTime(created, viewModel.getTerminalTimezone())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}