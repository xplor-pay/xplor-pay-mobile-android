package com.xplore.paymobile.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.xplore.paymobile.R
import com.xplore.paymobile.databinding.FragmentSettingsBinding
import com.xplore.paymobile.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : BaseFragment() {

    override val hasBottomNavigation: Boolean = true

    private val viewModel by viewModels<SettingsViewModel>()

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupViews()
    }

    private fun setupViews() {
        with(binding) {
            if (!viewModel.hasInternet) {
                merchantSelectFragment.isVisible = false
                noInternetWarning.isVisible = true
            } else {
                merchantSelectFragment.isVisible = true
                noInternetWarning.isVisible = false
            }
        }
    }

    private fun setupToolbar() {
        with(binding) {
            toolbarLayout.toolbarTitle.text = getString(R.string.title_settings)
            toolbarLayout.backButton.isVisible = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}