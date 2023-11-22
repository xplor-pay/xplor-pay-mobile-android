package com.xplore.paymobile.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import com.clearent.idtech.android.wrapper.ui.ClearentSDKActivity
import com.xplore.paymobile.R
import com.xplore.paymobile.databinding.FragmentSettingsBinding
import com.xplore.paymobile.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class SettingsFragment : BaseFragment() {

    override val hasBottomNavigation: Boolean = true

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

//    private val activityLauncher = registerForActivityResult(
//        ActivityResultContracts.StartActivityForResult()
//    ) { result ->
//        Timber.d(
//            "SDK UI result code: ${
//                result.data?.getIntExtra(ClearentSDKActivity.CLEARENT_RESULT_CODE, 0).toString()
//            }"
//        )
//    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
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