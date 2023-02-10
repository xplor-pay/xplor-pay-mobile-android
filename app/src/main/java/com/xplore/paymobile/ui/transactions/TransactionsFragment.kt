package com.xplore.paymobile.ui.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.xplore.paymobile.data.web.MerchantChangesEvents
import com.xplore.paymobile.data.web.WebEventsSharedViewModel
import com.xplore.paymobile.databinding.FragmentTransactionsBinding
import com.xplore.paymobile.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TransactionsFragment : BaseFragment() {

    override val hasBottomNavigation: Boolean = true

    private val viewModel by viewModels<TransactionsViewModel>()
    private val sharedViewModel by activityViewModels<WebEventsSharedViewModel>()

    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!

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

        setupViews()
        setupMerchantChangesEventsFlow()
    }

    private fun setupViews() {
        binding.apply {
            if (!viewModel.hasInternet) {
                webView.isVisible = false
                noInternetWarning.isVisible = true
                noEligibleTerminalWarning.isVisible = false
            } else if (viewModel.terminalAvailable()) {
                progressBar.isVisible = true
                noEligibleTerminalWarning.isVisible = false
                noInternetWarning.isVisible = false

                lifecycleScope.launch {
                    viewModel.prepareWebView(webView, requireContext(), sharedViewModel.jsBridge)
                    webView.reload()
                }
            } else {
                webView.isVisible = false
                progressBar.isVisible = false
                noEligibleTerminalWarning.isVisible = true
                noInternetWarning.isVisible = false
            }
        }
    }

    private fun setupMerchantChangesEventsFlow() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedViewModel.merchantChangesEventsFlow.collect { merchantChangesEvent ->
                    when (merchantChangesEvent) {
                        is MerchantChangesEvents.MerchantChanged -> sharedViewModel.wasMerchantChanged =
                            true
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}