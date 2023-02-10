package com.xplore.paymobile.ui.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.clearent.idtech.android.wrapper.ui.ClearentSDKActivity
import com.clearent.idtech.android.wrapper.ui.SdkUiResultCode
import com.xplore.paymobile.MainActivity
import com.xplore.paymobile.R
import com.xplore.paymobile.data.web.GroupedUserRoles
import com.xplore.paymobile.data.web.LoginEvents
import com.xplore.paymobile.data.web.WebEventsSharedViewModel
import com.xplore.paymobile.databinding.FragmentLoginBinding
import com.xplore.paymobile.interactiondetection.UserInteractionEvent
import com.xplore.paymobile.ui.dialog.BasicDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class LoginFragment : Fragment() {

    companion object {

        fun newInstance(onLoginSuccessful: () -> Unit) = LoginFragment().apply {
            viewModelBundle = {
                viewModel.onLoginSuccessful = onLoginSuccessful
            }
        }
    }

    private var viewModelBundle: (() -> Unit)? = null

    private val viewModel by viewModels<LoginViewModel>()
    private val sharedViewModel by activityViewModels<WebEventsSharedViewModel>()

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val activityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK)
            return@registerForActivityResult

        Timber.d(
            "Enable offline mode result ${
                result.data?.getIntExtra(
                    ClearentSDKActivity.CLEARENT_RESULT_CODE,
                    0
                )
            }"
        )

        val binaryAnd = result.data?.getIntExtra(ClearentSDKActivity.CLEARENT_RESULT_CODE, 0)
            ?.and(SdkUiResultCode.EnableOfflineSuccess.value)

        if (binaryAnd != 0) {
            findNavController().navigate(R.id.navigation_payment)
            viewModel.onLoginSuccessful()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModelBundle?.let { it() }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupViews()
        setupBypassLoginButton()
        checkInternetConnection()
    }

    private fun setupViewModel() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedViewModel.loginEventsFlow.collect { loginEvent ->
                    handleLoginEvents(loginEvent)
                }
            }
        }
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.interactionDetector.userInteractionFlow.collect { event ->
                    if (event is UserInteractionEvent.ExtendSession) {
                        viewModel.extendSession()
                    }
                }
            }
        }
    }

    private fun handleLoginEvents(loginEvent: LoginEvents) {
        when (loginEvent) {
            is LoginEvents.LoginSuccessful -> {
                viewModel.startVTRefreshTimer()
                viewModel.startInactivityTimer()
                sharedViewModel.allowLogout = true
                when (GroupedUserRoles.fromString(loginEvent.userRoles.roles)) {
                    GroupedUserRoles.VirtualTerminalRoles -> viewModel.onLoginSuccessful()
                    GroupedUserRoles.NoAccess -> BasicDialog(
                        getString(R.string.no_access_dialog_title),
                        getString(R.string.no_access_dialog_description)
                    ) { (requireActivity() as? MainActivity)?.logout() }.show(
                        parentFragmentManager,
                        BasicDialog::class.java.simpleName
                    )
                    GroupedUserRoles.MerchantHomeRoles -> {}
                }
            }
            else -> {}
        }
    }

    private fun setupViews() {
        binding.apply {
            viewModel.prepareWebView(webView, requireContext(), sharedViewModel.jsBridge)
        }
    }

    private fun setupBypassLoginButton() {
        binding.proceedButton.setOnClickListener {
            val intent = Intent(requireContext(), ClearentSDKActivity::class.java)
            intent.putExtra(
                ClearentSDKActivity.CLEARENT_ACTION_KEY,
                ClearentSDKActivity.CLEARENT_ACTION_ENABLE_OFFLINE_MODE
            )
            activityLauncher.launch(intent)
        }
    }

    private fun checkInternetConnection() {
        if (!viewModel.hasInternet && viewModel.hasTerminalSettings()) {
            with(binding) {
                webView.isVisible = false
                bypassLoginGroup.isVisible = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}