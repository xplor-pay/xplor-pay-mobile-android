package com.xplore.paymobile.ui.login

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.xplore.paymobile.MainActivity
import com.xplore.paymobile.R
import com.xplore.paymobile.data.web.GroupedUserRoles
import com.xplore.paymobile.data.web.LoginEvents
import com.xplore.paymobile.data.web.WebEventsSharedViewModel
import com.xplore.paymobile.databinding.FragmentLoginBinding
import com.xplore.paymobile.ui.dialog.BasicDialog
import com.xplore.paymobile.util.parcelable
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

@AndroidEntryPoint
class LoginFragment : Fragment() {

    companion object {

        private const val ON_LOGIN_SUCCESSFUL_KEY = "ON_LOGIN_SUCCESSFUL_KEY"

//        @JvmStatic
//        fun newInstance(onLoginSuccessful: () -> Unit) = LoginFragment().apply {
//            arguments = bundleOf(ON_LOGIN_SUCCESSFUL_KEY to OnLoginSuccessful(onLoginSuccessful))
//        }

        @JvmStatic
        fun newInstance(onLoginSuccessful: OnLoginSuccessful): LoginFragment {
            val myFragment = LoginFragment()

            val args = Bundle()
            args.putParcelable(ON_LOGIN_SUCCESSFUL_KEY, onLoginSuccessful)
            myFragment.arguments = args

            return myFragment
        }
    }

    private val viewModel by viewModels<LoginViewModel>()
    private val sharedViewModel by activityViewModels<WebEventsSharedViewModel>()

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupArguments()
    }

    private fun setupArguments() {
        arguments?.parcelable<OnLoginSuccessful>(ON_LOGIN_SUCCESSFUL_KEY)?.data?.also {
            viewModel.onLoginSuccessful = it
        }
        arguments?.remove(ON_LOGIN_SUCCESSFUL_KEY)
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
    }

    private fun setupViewModel() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedViewModel.loginEventsFlow.collect { loginEvent ->
                    handleLoginEvents(loginEvent)
                }
            }
        }
    }

    private fun handleLoginEvents(loginEvent: LoginEvents) {
        when (loginEvent) {
            is LoginEvents.LoginSuccessful -> {
                sharedViewModel.allowLogout = true
                when (GroupedUserRoles.fromString(loginEvent.userRoles.roles)) {
                    GroupedUserRoles.VirtualTerminalRoles -> viewModel.onLoginSuccessful()
                    GroupedUserRoles.NoAccess -> BasicDialog(
                        "",
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @Parcelize
    data class OnLoginSuccessful(val data: () -> Unit) : Parcelable
}