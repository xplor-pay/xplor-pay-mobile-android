package com.xplore.paymobile.ui.batches

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.xplore.paymobile.data.web.WebEventsSharedViewModel
import com.xplore.paymobile.databinding.FragmentBatchesBinding
import com.xplore.paymobile.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BatchesFragment : BaseFragment() {

    override val hasBottomNavigation: Boolean = true

    private val viewModel by viewModels<BatchesViewModel>()
    private val sharedViewModel by activityViewModels<WebEventsSharedViewModel>()

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

        setupViews()
    }

    private fun setupViews() {
        binding.apply {
            if (viewModel.terminalAvailable()) {
                webView.isVisible = true
                noEligibleTerminalWarning.isVisible = false

                viewModel.prepareWebView(webView, requireContext(), sharedViewModel.jsBridge)
                webView.reload()
            } else {
                webView.isVisible = false
                noEligibleTerminalWarning.isVisible = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}