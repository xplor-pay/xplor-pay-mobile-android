package com.xplore.paymobile.ui.more

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.clearent.idtech.android.wrapper.SDKWrapper
import com.xplore.paymobile.R
import com.xplore.paymobile.databinding.FragmentMoreBinding
import com.xplore.paymobile.util.Constants
import timber.log.Timber

class MoreFragment : Fragment() {

    companion object {
        private const val numberOfVisibleDigits = 0
    }

    private var _binding: FragmentMoreBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoreBinding.inflate(inflater, container, false)

        populateUI()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun populateUI() {
        binding.apply {
            viewLogs.setOnClickListener {
                findNavController().navigate(R.id.action_navigation_more_to_logsFragment)
            }
            deleteLogs.setOnClickListener {
                SDKWrapper.deleteLogs()
                Toast.makeText(requireContext(), "Logs deleted", Toast.LENGTH_SHORT).show()
            }
            shareLogs.setOnClickListener {
                shareLogsFile()
            }

            urlText.text = Constants.BASE_URL_SANDBOX
            publicKeyText.text = hideKey(Constants.PUBLIC_KEY_SANDBOX)
            apiKeyText.text = hideKey(Constants.API_KEY_SANDBOX)
            switchButton.setOnCheckedChangeListener { _, isChecked ->
                prodLabel.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        if (isChecked) R.color.teal_200 else R.color.black
                    )
                )
                sandboxLabel.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        if (isChecked) R.color.black else R.color.teal_200
                    )
                )
                urlText.text =
                    if (isChecked) Constants.BASE_URL_PROD else Constants.BASE_URL_SANDBOX
                publicKeyText.text =
                    if (isChecked) hideKey(Constants.PUBLIC_KEY_PROD) else hideKey(Constants.PUBLIC_KEY_SANDBOX)
                apiKeyText.text =
                    if (isChecked) hideKey(Constants.API_KEY_PROD) else hideKey(Constants.API_KEY_SANDBOX)
            }
        }
    }

    private fun shareLogsFile() {
        val senderIntent = Intent(Intent.ACTION_SEND)

        // Get the file we want to share
        val file = SDKWrapper.getLogFile(requireContext())

        // Try to retrieve the uri of the file
        val fileUri: Uri? = try {
            FileProvider.getUriForFile(
                requireContext(),
                "com.xplore.paymobile.fileprovider",
                file
            )
        } catch (e: IllegalArgumentException) {
            Timber.e("The selected file can't be shared: $file - check authority.")
            return
        }

        fileUri?.also {
            // Grant temporary read permission to the content URI
            senderIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            // Put the Uri and MIME type in the result Intent
            senderIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
            senderIntent.type = requireContext().contentResolver.getType(fileUri)
            // Choose how you want to share the file
            requireContext().startActivity(Intent.createChooser(senderIntent, null))
        } ?: run {
            Toast.makeText(
                context,
                "Could not retrieve file uri for file: ${file.name}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun hideKey(key: String) =
        getString(R.string.hidden_key, key.takeLast(numberOfVisibleDigits))
}