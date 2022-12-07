package com.xplore.paymobile.ui.settings

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
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.clearent.idtech.android.wrapper.ClearentWrapper
import com.xplore.paymobile.BuildConfig
import com.xplore.paymobile.R
import com.xplore.paymobile.databinding.FragmentSettingsBinding
import com.xplore.paymobile.ui.dialog.BasicDialog
import com.xplore.paymobile.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    companion object {
        private const val numberOfVisibleDigits = 0
    }

    private val viewModel by viewModels<SettingsViewModel>()

    private var _binding: FragmentSettingsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        populateUI()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun populateUI() {
        binding.apply {
            saveKeys.setOnClickListener {
                val apiKey = apiKeyTextInput.editText?.text?.toString() ?: ""
                val publicKey = publicKeyTextInput.editText?.text?.toString() ?: ""
                viewModel.setApiKey(apiKey)
                viewModel.setPublicKey(publicKey)
                if (ClearentWrapper.currentReader?.isConnected == true) {
                    ClearentWrapper.disconnect()
                }
                ClearentWrapper.initializeSDK(
                    requireContext(),
                    if (switchButton.isChecked) Constants.BASE_URL_PROD else Constants.BASE_URL_SANDBOX,
                    publicKey,
                    apiKey
                )
                BasicDialog(
                    getString(R.string.keys_alert_title),
                    getString(R.string.keys_update_message)
                ).show(parentFragmentManager, BasicDialog::class.java.simpleName)
            }
            viewLogs.setOnClickListener {
                findNavController().navigate(R.id.action_navigation_settings_to_logsFragment)
            }
            deleteLogs.setOnClickListener {
                ClearentWrapper.deleteLogs()
                Toast.makeText(requireContext(), "Logs deleted", Toast.LENGTH_SHORT).show()
            }
            shareLogs.setOnClickListener {
                shareLogsFile()
            }

            apiKeyTextInput.editText?.setText(viewModel.getApiKey())
            publicKeyTextInput.editText?.setText(viewModel.getPublicKey())

            versionNumber.text = getString(R.string.version_number_format, BuildConfig.VERSION_NAME)

            urlText.text = Constants.BASE_URL_SANDBOX
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
            }
        }
    }

    private fun shareLogsFile() {
        val senderIntent = Intent(Intent.ACTION_SEND)

        // Get the file we want to share
        val file = ClearentWrapper.getLogFile(requireContext())

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