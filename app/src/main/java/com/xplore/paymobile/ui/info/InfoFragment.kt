package com.xplore.paymobile.ui.info

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.xplore.paymobile.BuildConfig
import com.xplore.paymobile.MainActivity
import com.xplore.paymobile.R
import com.xplore.paymobile.data.datasource.SharedPreferencesDataSource
import com.xplore.paymobile.databinding.FragmentInfoBinding
import com.xplore.paymobile.ui.PhoneUtils
import com.xplore.paymobile.ui.base.BaseFragment
import com.xplore.paymobile.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class InfoFragment : BaseFragment() {

    override val hasBottomNavigation: Boolean = true

    @Inject
    lateinit var sharedPrefs: SharedPreferencesDataSource

    private var _binding: FragmentInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTextViews()
        setupClickListeners()
        (requireActivity() as? MainActivity)?.checkForAppUpdate { binding.updateButton.isEnabled = true }
    }

    private fun setupTextViews() {
        binding.apply {
            callMessage.text =
                getString(R.string.info_screen_call_message, Constants.CLIENT_SUPPORT_PHONE_NUMBER)
            appVersion.text = getString(R.string.info_screen_app_version, BuildConfig.VERSION_NAME)
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            callButton.setOnClickListener {
                PhoneUtils.dialNumber(requireContext(), Constants.CLIENT_SUPPORT_PHONE_NUMBER)
            }
            termsAndConditionsButton.setOnClickListener {
                openTermsAndConditionsLink()
            }
            updateButton.setOnClickListener {
                (requireActivity() as? MainActivity)?.updateApp()
            }
            logOutButton.setOnClickListener {
                sharedPrefs.setAuthToken(null)
                (requireActivity() as? MainActivity)?.logout()
            }
        }
    }

    private fun openTermsAndConditionsLink() {
        val linkIntent = Intent(Intent.ACTION_VIEW)
        linkIntent.data = Uri.parse(Constants.TERMS_AND_CONDITIONS_LINK)
        startActivity(linkIntent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}