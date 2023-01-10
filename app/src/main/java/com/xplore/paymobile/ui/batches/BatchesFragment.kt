package com.xplore.paymobile.ui.batches

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.xplore.paymobile.databinding.FragmentBatchesBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BatchesFragment : Fragment() {

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

        setupViews()
    }

    private fun setupViews() {
        binding.apply {
            viewModel.prepareWebView(webView, requireContext())
            webView.reload()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}