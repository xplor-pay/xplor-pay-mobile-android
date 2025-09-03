package com.xplore.paymobile.ui.merchantselection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.xplore.paymobile.MainActivity
import com.xplore.paymobile.databinding.FragmentPostLoginMerchantSelectBinding
import com.xplore.paymobile.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PostLoginMerchantSelectFragment : BaseFragment() {

    private var _binding: FragmentPostLoginMerchantSelectBinding? = null
    private val binding get() = _binding!!

    override val hasBottomNavigation: Boolean = false

    private val sharedViewModel by activityViewModels<MerchantSelectSharedViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentPostLoginMerchantSelectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.nextButton.setOnClickListener {
            (activity as? MainActivity)?.navigateToBottomNavItemSelected()
        }
        lifecycleScope.launch {
            sharedViewModel.allowNext.collect { allow ->
                _binding?.also {
                    binding.nextButton.isEnabled = allow
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
