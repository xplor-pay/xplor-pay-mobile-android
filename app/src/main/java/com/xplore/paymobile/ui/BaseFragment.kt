package com.xplore.paymobile.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.xplore.paymobile.ActivityViewModel
import com.xplore.paymobile.LoginEvents
import com.xplore.paymobile.MainActivity
import com.xplore.paymobile.R
import com.xplore.paymobile.ui.dialog.BasicDialog
import kotlinx.coroutines.launch

open class BaseFragment : Fragment() {

    open val viewModel by viewModels<ActivityViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginEventsFlow.collect { loginEvent ->
                    when (loginEvent) {
                        LoginEvents.Logout -> BasicDialog(
                            getString(R.string.logout_dialog_title),
                            getString(R.string.logout_dialog_description)
                        ) { (activity as MainActivity).logout() }.show(
                            parentFragmentManager,
                            BasicDialog::class.java.simpleName
                        )
                    }
                }
            }
        }
    }
}