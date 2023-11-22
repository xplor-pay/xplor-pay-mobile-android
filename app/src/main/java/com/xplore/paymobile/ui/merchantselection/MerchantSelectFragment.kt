package com.xplore.paymobile.ui.merchantselection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.xplore.paymobile.R
import com.xplore.paymobile.data.remote.model.Terminal
import com.xplore.paymobile.data.web.Merchant
import com.xplore.paymobile.databinding.FragmentMerchantSelectBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class MerchantSelectFragment : Fragment() {

    private var _binding: FragmentMerchantSelectBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<MerchantSelectViewModel>()
    private val sharedViewModel by activityViewModels<MerchantSelectSharedViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMerchantSelectBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupButtons()
        setupFlows()
    }

    private fun setupFlows() {
        setupMerchantFlow()
        setupTerminalFlow()
        setupLoadingFlow()
    }

    private fun setupLoadingFlow() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loadingFlow.collect { isLoading ->
                    showLoading(isLoading)
                    when (isLoading) {
                        true -> {
                            showNoTerminalsWarning(false)
                            disableInputs()
                            sharedViewModel.setAllowNext(false)
                        }
                        false -> {
                            binding.merchantClickArea.isClickable = true
                            checkEdgeCases()
                        }
                    }
                }
            }
        }
    }

    private fun disableInputs() {
        binding.terminalClickArea.isClickable = false
        binding.merchantClickArea.isClickable = false
    }

    private fun checkEdgeCases() {
        val selectedMerchant = viewModel.getMerchant()
        val selectedTerminal = viewModel.getTerminal()
        val allTerminals = sharedViewModel.terminals
//        if (selectedMerchant != null && viewModel.isLoggedIn()) {
        if (selectedMerchant != null) {
            when {
                allTerminals.isEmpty() && selectedTerminal == null -> {
                    showNoTerminalsWarning(true)
                    sharedViewModel.setAllowNext(false)
                    binding.terminalClickArea.isClickable = false
                }
                selectedTerminal == null -> {
                    sharedViewModel.setAllowNext(false)
                    binding.terminalClickArea.isClickable = true
                }
                allTerminals.size == 1 && allTerminals[0].terminalPKId == selectedTerminal.terminalPKId -> {
                    showNoTerminalsWarning(false)
                    sharedViewModel.setAllowNext(true)
                    binding.terminalClickArea.isClickable = true
                }
                else -> {
                    showNoTerminalsWarning(false)
                    sharedViewModel.setAllowNext(true)
                    binding.terminalClickArea.isClickable = true
                }
            }
        } else {
            showNoTerminalsWarning(false)
            sharedViewModel.setAllowNext(false)
            binding.terminalClickArea.isClickable = false
        }
    }

    private fun showNoTerminalsWarning(show: Boolean) {
        binding.noTerminals.isVisible = show
    }

    private fun showLoading(loading: Boolean) {
        binding.progressBar.isVisible = loading
    }

    private fun setupTerminalFlow() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.selectedTerminalFlow.collectLatest { terminalSelection ->
                        when (terminalSelection) {
                            is TerminalSelection.TerminalAvailable -> {
                                Timber.d("Selected terminal is ${terminalSelection.terminal.terminalName}")
                                Timber.d("Selected terminal id ${terminalSelection.terminal.terminalPKId}")
                                setupTerminalName(terminalSelection.terminal)
                            }
                            is TerminalSelection.NoTerminal -> {
                                Timber.d("No terminal selected")
                                setupTerminalName(null)
                            }
                        }
                    }
                }
                launch {
                    viewModel.terminalsFlow.collect { terminals ->
                        Timber.d("Received terminals $terminals")
                        if (terminals.isEmpty()) {
                            binding.noTerminals.isVisible = true
                        }
                        sharedViewModel.terminals = terminals
                    }
                }
            }
        }
    }

    private fun setupMerchantFlow() {
        lifecycleScope.launchWhenStarted {
            viewModel.merchantFlow.collect { merchant ->
                Timber.d("Selected merchant is ${merchant?.merchantName}")
                setupMerchantName(merchant)
            }
        }
    }

    private fun setupMerchantName(merchant: Merchant?) {
        with(binding) {
            merchant?.merchantName?.let {
                merchantTextLayout.editText?.setText(it)
            } ?: run {
                merchantTextLayout.editText?.setText(getString(R.string.select_merchant))
            }
        }
    }

    private fun setupTerminalName(terminal: Terminal?) {
        with(binding) {
            terminal?.terminalName?.also {
                terminalTextLayout.editText?.setText(it)
            } ?: run {
                terminalTextLayout.editText?.setText(getString(R.string.select_terminal))
            }
        }
    }

    private fun setupButtons() {
        with(binding) {
            merchantClickArea.setOnClickListener {
                findNavController().navigate(R.id.merchant_search_fragment)
            }
            terminalClickArea.setOnClickListener {
                findNavController().navigate(R.id.terminal_search_fragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}