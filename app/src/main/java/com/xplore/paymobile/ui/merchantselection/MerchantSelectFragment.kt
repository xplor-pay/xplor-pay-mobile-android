package com.xplore.paymobile.ui.merchantselection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.xplore.paymobile.R
import com.xplore.paymobile.databinding.FragmentMerchantSelectBinding
import com.xplore.paymobile.ui.merchantselection.search.merchant.MerchantSearchViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MerchantSelectFragment : Fragment() {

    private var _binding: FragmentMerchantSelectBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<MerchantSearchViewModel>()
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
    }

    private fun setupButtons() {
        with(binding) {
            merchantLayout.setOnClickListener {
                findNavController().navigate(R.id.merchant_search_fragment)
            }
            terminalLayout.setOnClickListener {
                findNavController().navigate(R.id.terminal_search_fragment, bundleOf("merchantId" to "MERCHANT_ID"))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}