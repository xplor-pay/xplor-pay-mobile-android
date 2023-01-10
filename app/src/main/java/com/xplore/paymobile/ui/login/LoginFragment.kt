package com.xplore.paymobile.ui.login

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.xplore.paymobile.databinding.FragmentLoginBinding
import com.xplore.paymobile.util.parcelable
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

@AndroidEntryPoint
class LoginFragment : Fragment() {

    companion object {

        private const val ON_LOGIN_SUCCESSFUL_KEY = "ON_LOGIN_SUCCESSFUL_KEY"

        fun newInstance(onLoginSuccessful: () -> Unit) = LoginFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ON_LOGIN_SUCCESSFUL_KEY, OnLoginSuccessful(onLoginSuccessful))
            }
        }

        @Parcelize
        data class OnLoginSuccessful(val data: () -> Unit) : Parcelable
    }

    private val viewModel by viewModels<LoginViewModel>()

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.parcelable<OnLoginSuccessful>(ON_LOGIN_SUCCESSFUL_KEY)?.data?.also {
            viewModel.onLoginSuccessful = it
        }
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
                viewModel.loginEventsFlow.collect { loginEvent ->
                    handleLoginEvents(loginEvent)
                }
            }
        }
    }

    private fun handleLoginEvents(loginEvent: LoginViewModel.LoginEvents) {
        when (loginEvent) {
            LoginViewModel.LoginEvents.LoginSuccessful -> viewModel.onLoginSuccessful()
        }
    }

    private fun setupViews() {
        binding.apply {
            viewModel.prepareWebView(webView, requireContext())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}