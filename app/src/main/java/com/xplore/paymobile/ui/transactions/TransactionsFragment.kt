package com.xplore.paymobile.ui.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.clearent.idtech.android.wrapper.http.model.TransactionType
import com.clearent.idtech.android.wrapper.ui.util.MarginItemDecoration
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.xplore.paymobile.R
import com.xplore.paymobile.data.remote.model.Transaction
import com.xplore.paymobile.databinding.FragmentTransactionsBinding
import com.xplore.paymobile.ui.base.BaseFragment
import com.xplore.paymobile.ui.transactions.adapter.TransactionListAdapter
import com.xplore.paymobile.ui.transactions.util.DateFormatUtil
import com.xplore.paymobile.util.Logger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class TransactionsFragment : BaseFragment() {

    private val className: String = "TransactionsFragment"

    private val viewModel by viewModels<TransactionsViewModel>()
    private var transactionLinearLayout: LinearLayoutManager? = null

    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!

    private val transactionListAdapter =
        TransactionListAdapter(onItemClicked = { transactionItem, _ ->
            if (transactionItem.status != "Declined" && transactionItem.status != "Error") {
                val processType = determineTransactionProcessType(
                    transactionItem.type,
                    transactionItem.settled,
                    transactionItem.pending
                )

                if (processType.isNotBlank() && viewModel.hasVoidAndRefundPermissions() && !transactionItem.voided) {
                    Logger.logMobileMessage(className,"Attempting to process transaction id: ${transactionItem.id}")
                    showProcessTransactionDialog(transactionItem, processType)
                }
            }
        })

    private fun determineTransactionProcessType(
        transactionType: String?,
        isSettled: Boolean,
        isPending: Boolean
    ): String {
        return if (isSettled && transactionType != "REFUND" && transactionType != "UNMATCHED REFUND" && transactionType != "AUTH") {
            "REFUND"
        } else if ((!isSettled || (transactionType == "AUTH" && isPending)) && transactionType != "REFUND") {
            "VOID"
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
        setupTitle()
        setupLoadingFlow()
        viewModel.nextPage()
        setTransactionListAdapter()
        setOnScrollListener()
        setupRefreshListener()
        submitTransactionList()
    }

    private fun setupRefreshListener() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshPage()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun setOnScrollListener() {
        binding.transactionItemsList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (transactionListAdapter.getCurrentScrollPosition() == transactionListAdapter.currentList.size - 20) {
                    if (!viewModel.isLoading() && !viewModel.isLastTransactionPage()) {
                        viewModel.nextPage()
                    }
                }
            }
        })
    }

    private fun submitTransactionList() {
        lifecycleScope.launch {
            viewModel.resultsFlow.collect { transactionList ->
                Timber.d("Received transactions ${transactionList.size}")
                submitList(transactionList)
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

    //todo move this method into the process transaction class
    private fun showProcessTransactionDialog(
        transactionItem: TransactionListAdapter.TransactionItem,
        transactionType: String
    ) {
        val processTransactionDialog = BottomSheetDialog(requireContext())
        processTransactionDialog.setContentView(R.layout.process_transaction)

        transactionLinearLayout = LinearLayoutManager(requireContext())

        processTransactionDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED

        val cancelButton = processTransactionDialog.findViewById<Button>(R.id.cancel_button)
        val processTransactionButton =
            processTransactionDialog.findViewById<Button>(R.id.process_transaction_button)
        processTransactionButton?.text = transactionType

        processTransactionDialog.findViewById<TextView>(R.id.process_amount)?.text =
            buildString {
                append("$")
                append(transactionItem.amount)
            }
        processTransactionDialog.findViewById<TextView>(R.id.process_created)?.text = transactionItem.created

        cancelButton?.setOnClickListener {
            Logger.logMobileMessage(className,"User cancelled processing of transaction id: ${transactionItem.id}")
            processTransactionDialog.dismiss()
        }

        processTransactionButton?.setOnClickListener {
            Logger.logMobileMessage(className,"User clicked $transactionType button: ${transactionItem.id}")
            processTransaction(transactionItem, transactionType)
            processTransactionDialog.dismiss()
        }

        processTransactionDialog.setCanceledOnTouchOutside(true)
        processTransactionDialog.setCancelable(true)

        processTransactionDialog.show()
    }

    //todo move this method into the process transaction class (make adapter with onclick behavior)
    private fun processTransaction(transactionItem: TransactionListAdapter.TransactionItem, transactionType: String) {

        lifecycleScope.launch {
            viewModel.processTransaction(transactionItem, transactionType)
            //todo let's see if we can reduce the delay time
            delay(3500)
            showDialogMessage(transactionType)
        }
    }

    private fun showDialogMessage(type: String) {
        if(viewModel.isProcessTransactionSuccessful()) {
            showSuccessMessage(type)
            viewModel.refreshPage()
        } else {
            showErrorMessage()
        }
    }

    private fun showSuccessMessage(type: String) {
        with(binding) {
            if (type.uppercase() == TransactionType.VOID.name) {
                successMessage.text = getString(R.string.transaction_voided)
            } else {
                successMessage.text = getString(R.string.transaction_refunded)
            }
            successMessageLayout.isVisible = true
            val animSlideDown = AnimationUtils.loadAnimation(requireContext(), R.anim.vertical_slide_down_up);
            successMessageLayout.startAnimation(animSlideDown)
            successMessageLayout.isVisible = false

        }
    }

    private fun showErrorMessage() {
        with(binding) {
            errorMessageLayout.isVisible = true
            val animSlideDown =
                AnimationUtils.loadAnimation(requireContext(), R.anim.vertical_slide_down_up);
            errorMessageLayout.startAnimation(animSlideDown)
            errorMessageLayout.isVisible = false

        }
    }

    private fun setupTitle() {
        with(binding) {
            toolbarLayout.toolbarTitle.text = getString(R.string.transactions_title)
            toolbarLayout.backButton.setOnClickListener {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun setTransactionListAdapter() {
        binding.apply {
            transactionItemsList.adapter = concatAdapter
            transactionItemsList.layoutManager = LinearLayoutManager(requireContext())
            transactionItemsList.addItemDecoration(MarginItemDecoration(40, 20))
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
                it.pending,
                it.voided
            )
        }) {
            if (viewModel.currentPage() == 0 && transactionItemList.isNotEmpty()) {
                binding.transactionItemsList.scrollToPosition(0)
            }
        }
    }

    private fun formatCreatedDate(created: String): String? {
        return viewModel.getTerminalTimezone().let { DateFormatUtil.formatDateTime(created, it) }
    }

    private fun showLoading(loading: Boolean) {
        binding.progressBar.isVisible = loading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}