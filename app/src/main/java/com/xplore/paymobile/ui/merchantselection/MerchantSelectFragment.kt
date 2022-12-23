package com.xplore.paymobile.ui.merchantselection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.xplore.paymobile.R
import com.xplore.paymobile.data.remote.model.Merchant
import com.xplore.paymobile.data.remote.model.Terminal
import com.xplore.paymobile.databinding.FragmentMerchantSelectBinding
import dagger.hilt.android.AndroidEntryPoint
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
            viewModel.loadingFlow.collect { isLoading ->
                showLoading(isLoading)
                when (isLoading) {
                    true -> {
                        showNoTerminalsWarning(false)
                        disableInputs()
                        sharedViewModel.setAllowNext(false)
                    }
                    false -> {
                        binding.merchantLayout.isClickable = true
                        checkEdgeCases()
                    }
                }
            }
        }
    }

    private fun disableInputs() {
        binding.terminalLayout.isClickable = false
        binding.merchantLayout.isClickable = false
    }

    private fun checkEdgeCases() {
        val selectedMerchant = viewModel.getMerchant()
        val selectedTerminal = viewModel.getTerminal()
        val allTerminals = sharedViewModel.terminals
        if (selectedMerchant != null) {
            when {
                allTerminals.size == 1 && allTerminals[0].terminalPKId == selectedTerminal?.terminalPKId -> {
                    showNoTerminalsWarning(false)
                    sharedViewModel.setAllowNext(true)
                    binding.terminalLayout.isClickable = false
                }
                allTerminals.isEmpty() && selectedTerminal == null -> {
                    showNoTerminalsWarning(true)
                    sharedViewModel.setAllowNext(true)
                    binding.terminalLayout.isClickable = false
                }
                selectedTerminal == null -> {
                    sharedViewModel.setAllowNext(false)
                    binding.terminalLayout.isClickable = true
                }
                else -> {
                    showNoTerminalsWarning(false)
                    sharedViewModel.setAllowNext(true)
                    binding.terminalLayout.isClickable = true
                }
            }
        } else {
            showNoTerminalsWarning(false)
            sharedViewModel.setAllowNext(false)
            binding.terminalLayout.isClickable = false
        }
    }

    private fun showNoTerminalsWarning(show: Boolean) {
        binding.noTerminalsTextView.isVisible = show
    }

    private fun showLoading(loading: Boolean) {
        binding.progressBar.isVisible = loading
    }

    private fun setupTerminalFlow() {
        lifecycleScope.launch {
            viewModel.selectedTerminalFlow.collect { terminal ->
                Timber.d("TESTEST terminal ${terminal?.terminalName}")
                setupTerminalName(terminal)
            }
        }
        lifecycleScope.launch {
            viewModel.terminalsFlow.collect { terminals ->
                Timber.d("TESTEST terminals $terminals")
                sharedViewModel.terminals = terminals
            }
        }
    }

    private fun setupMerchantFlow() {
        lifecycleScope.launch {
            viewModel.merchantFlow.collect { merchant ->
                Timber.d("TESTEST merchant ${merchant?.merchantName}")
                setupMerchantName(merchant)
            }
        }
    }

    private fun setupMerchantName(merchant: Merchant?) {
        with(binding) {
            merchantTextView.text = merchant?.merchantName ?: getString(R.string.merchant)
        }
    }

    private fun setupTerminalName(terminal: Terminal?) {
        with(binding) {
            terminalTextView.text = terminal?.terminalName ?: getString(R.string.terminal)
        }
    }

    private fun setupButtons() {
        with(binding) {
            merchantLayout.setOnClickListener {
                findNavController().navigate(R.id.merchant_search_fragment)
            }
            terminalLayout.setOnClickListener {
                findNavController().navigate(R.id.terminal_search_fragment)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchMerchantAndTerminal()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}