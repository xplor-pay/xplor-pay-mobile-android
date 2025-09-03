package com.xplore.paymobile.ui

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.xplore.paymobile.R
import com.xplore.paymobile.databinding.FragmentNoEligibleTerminalBinding
import com.xplore.paymobile.util.Constants

class NoEligibleTerminalFragment : Fragment() {

    private var _binding: FragmentNoEligibleTerminalBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentNoEligibleTerminalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            val contactNumber = getString(R.string.no_eligible_terminal_available_contact_number)
            val noEligibleTerminal =
                getString(R.string.no_eligible_terminal_available, contactNumber)
            val noEligibleTerminalString = SpannableString(noEligibleTerminal)
            val clickableString = object : ClickableSpan() {
                override fun onClick(p0: View) {
                    PhoneUtils.dialNumber(requireContext(), Constants.CLIENT_SUPPORT_PHONE_NUMBER)
                }
            }
            val contactNumberRange = contactNumber.toRegex().find(noEligibleTerminal)
            contactNumberRange?.range?.also { range ->
                noEligibleTerminalString.setSpan(
                    clickableString,
                    range.first,
                    range.last + 1,
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE,
                )
            }

            noEligibleTerminalText.text = noEligibleTerminalString
            noEligibleTerminalText.movementMethod = LinkMovementMethod.getInstance()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
