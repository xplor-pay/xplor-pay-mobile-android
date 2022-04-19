package com.xplore.paymobile.ui.more

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.xplore.paymobile.R
import com.xplore.paymobile.databinding.FragmentMoreBinding
import com.xplore.paymobile.util.Constants

class MoreFragment : Fragment() {

    private var _binding: FragmentMoreBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val moreViewModel =
            ViewModelProvider(this).get(MoreViewModel::class.java)

        _binding = FragmentMoreBinding.inflate(inflater, container, false)
        val root: View = binding.root

        populateUI()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun populateUI() {
        binding.urlText.text = Constants.BASE_URL_SANDBOX
        binding.publicKeyText.text = Constants.PUBLIC_KEY_SANDBOX
        binding.apiKeyText.text = Constants.API_KEY_SANDBOX
        binding.switchButton.setOnCheckedChangeListener { view, isChecked ->
            binding.prodLabel.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    if (isChecked) R.color.teal_200 else R.color.black
                )
            )
            binding.sandboxLabel.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    if (isChecked) R.color.black else R.color.teal_200
                )
            )
            binding.urlText.text =
                if (isChecked) Constants.BASE_URL_PROD else Constants.BASE_URL_SANDBOX
            binding.publicKeyText.text =
                if (isChecked) Constants.PUBLIC_KEY_PROD else Constants.PUBLIC_KEY_SANDBOX
            binding.apiKeyText.text =
                if (isChecked) Constants.API_KEY_PROD else Constants.API_KEY_SANDBOX
        }
    }
}