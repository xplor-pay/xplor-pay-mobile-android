package com.xplore.paymobile.ui.merchantselection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.xplore.paymobile.R
import com.xplore.paymobile.databinding.FragmentPostLoginMerchantSelectBinding
import com.xplore.paymobile.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import java.math.BigDecimal

@AndroidEntryPoint
class PostLoginMerchantSelectFragment : BaseFragment() {

    private var _binding: FragmentPostLoginMerchantSelectBinding? = null
    private val binding get() = _binding!!

    override val hasBottomNavigation: Boolean = false

    private val viewModel by viewModels<PostLoginMerchantSelectViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostLoginMerchantSelectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.nextButton.setOnClickListener {
            findNavController().navigate(R.id.action_post_login_to_payment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}