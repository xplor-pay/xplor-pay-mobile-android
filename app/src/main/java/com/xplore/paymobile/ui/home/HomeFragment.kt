package com.xplore.paymobile.ui.home

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.clearent.idtech.android.wrapper.ClearentWrapper
import com.clearent.idtech.android.wrapper.listener.OfflineModeEnabledListener
import com.clearent.idtech.android.wrapper.listener.ReaderStatusListener
import com.clearent.idtech.android.wrapper.model.*
import com.clearent.idtech.android.wrapper.ui.ClearentAction
import com.clearent.idtech.android.wrapper.ui.ClearentSDKActivity
import com.clearent.idtech.android.wrapper.ui.ClearentSDKActivity.Companion.CLEARENT_RESULT_CODE
import com.clearent.idtech.android.wrapper.ui.PaymentMethod
import com.clearent.idtech.android.wrapper.ui.SdkUiResultCode
import com.xplore.paymobile.R
import com.xplore.paymobile.databinding.FragmentHomeBinding
import com.xplore.paymobile.ui.FirstPairListener
import com.xplore.paymobile.ui.dialog.BasicDialog
import com.xplore.paymobile.util.SharedPreferencesDataSource.FirstPair
import com.xplore.paymobile.util.insert
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class HomeFragment : Fragment(), ReaderStatusListener, OfflineModeEnabledListener {

    companion object {
        private const val DEFAULT_CHARGE_AMOUNT = "$0.00"
    }

    private val viewModel by viewModels<HomeViewModel>()

    private val clearentWrapper = ClearentWrapper.getInstance()

    private var chargeAmount = ""
    private var transactionOngoing = false
    private var shouldShowSignature = true

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val activityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Timber.d(
            "SDK UI result code: ${
                result.data?.getIntExtra(CLEARENT_RESULT_CODE, 0).toString()
            }"
        )

        transactionOngoing = false

        if (result.resultCode != Activity.RESULT_OK)
            return@registerForActivityResult

        if (result.data?.getIntExtra(CLEARENT_RESULT_CODE, 0)
                ?.and(SdkUiResultCode.TransactionSuccess.value) != 0
        ) clearChargeAmount()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (viewModel.shouldShowHints())
            showHints()

        setNumericKeyPadBackground()
        handlePaymentMethodButtonState()
        setupPaymentMethodClickListeners()
        renderChargeAmount()
        setUpNumpad()
        setListeners()
        clearentWrapper.addReaderStatusListener(this)
        clearentWrapper.addOfflineModeEnabledListener(this)
    }

    private fun setupPaymentMethodClickListeners() {
        with(binding) {
            cardReaderButton.setOnClickListener { handlePaymentMethodButtonPressed(true) }
            manualEntryButton.setOnClickListener { handlePaymentMethodButtonPressed(false) }
        }
    }

    private fun handlePaymentMethodButtonPressed(isCardReader: Boolean) {
        viewModel.isCardReaderSelected = isCardReader
        handlePaymentMethodButtonState()
    }

    private fun handlePaymentMethodButtonState() {
        val isCardReader = viewModel.isCardReaderSelected
        with(binding) {
            val cardReaderBgColor =
                if (isCardReader) R.color.button_enabled else R.color.gray
            val manualEntryBgColor =
                if (!isCardReader) R.color.button_enabled else R.color.gray
            val cardReaderTextColor =
                if (isCardReader) R.color.button_enabled else R.color.black
            val manualEntryTextColor =
                if (!isCardReader) R.color.button_enabled else R.color.black
            cardReaderButton.strokeColor = ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    cardReaderBgColor
                )
            )
            cardReaderButton.setTextColor(requireContext().getColor(cardReaderTextColor))
            manualEntryButton.strokeColor = ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    manualEntryBgColor
                )
            )
            manualEntryButton.setTextColor(requireContext().getColor(manualEntryTextColor))
        }
    }

    private fun showHints() = lifecycleScope.launch {
        val listener = activity as? FirstPairListener

        listener?.also {
            it.showFirstPair(
                { startPairingProcess() },
                { viewModel.firstPairSkipped() }
            )
        }
    }

    private fun renderCurrentReader(readerStatus: ReaderStatus?) {
        if (viewModel.getFirstPair() == FirstPair.DONE) {
            renderReader(readerStatus)
        } else {
            readerStatus?.also {
                viewModel.firstPairDone()
                renderReader(it)
            } ?: run {
                binding.firstReader.visibility = View.VISIBLE
                binding.readerInfo.root.visibility = View.GONE
            }
        }
    }

    private fun renderReader(readerStatus: ReaderStatus?) {
        binding.firstReader.visibility = View.GONE
        binding.readerInfo.root.visibility = View.VISIBLE
        setReaderState(ReaderState.fromReaderStatus(readerStatus))
    }

    private fun setNumericKeyPadBackground() {
        binding.numpad.root.setBackgroundResource(R.drawable.bg_numeric_key_pad)
    }

    private fun setUpNumpad() {
        binding.apply {
            numpad.apply {
                val numericKeys = listOf(
                    numpad0,
                    numpad1,
                    numpad2,
                    numpad3,
                    numpad4,
                    numpad5,
                    numpad6,
                    numpad7,
                    numpad8,
                    numpad9
                )
                numericKeys.forEachIndexed { index, key ->
                    key.setOnClickListener {
                        appendDigitToChargeAmount(index.toString())
                    }
                }

                numpadClear.setOnClickListener {
                    clearChargeAmount()
                }

                numpadBackspace.setOnClickListener {
                    popDigitFromChargeAmount()
                }
            }
        }
    }

    private fun setListeners() {
        binding.apply {
            readerInfo.root.setOnClickListener {
                openSettings()
            }
            firstReader.setOnClickListener {
                startPairingProcess()
            }
            chargeButton.setOnClickListener {
                startSdkActivityForResult(
                    ClearentAction.Transaction(
                        PaymentInfo(chargeAmount.toDouble() / 100),
                        viewModel.shouldShowHints(),
                        shouldShowSignature,
                        if (viewModel.isCardReaderSelected) PaymentMethod.CARD_READER else PaymentMethod.MANUAL_ENTRY
                    )
                )
            }
            settingsButton.setOnClickListener {
                openSettings()
            }
        }
    }

    private fun startPairingProcess() =
        startSdkActivityForResult(ClearentAction.Pairing(viewModel.shouldShowHints()))

    private fun openSettings() =
        startSdkActivityForResult(ClearentAction.Settings)

    private fun startSdkActivityForResult(clearentAction: ClearentAction) {
        if (clearentAction is ClearentAction.Transaction && (viewModel.getApiKey()
                .isEmpty() || viewModel.getPublicKey().isEmpty())
        ) {
            BasicDialog(
                getString(R.string.keys_alert_title),
                getString(R.string.keys_alert_message)
            ).show(parentFragmentManager, BasicDialog::class.java.simpleName)
            return
        }
        if (transactionOngoing)
            return

        transactionOngoing = true

        val intent = Intent(requireContext(), ClearentSDKActivity::class.java)
        when (clearentAction) {
            is ClearentAction.Pairing -> {
                intent.putExtra(
                    ClearentSDKActivity.CLEARENT_ACTION_KEY,
                    ClearentSDKActivity.CLEARENT_ACTION_PAIR
                )
                intent.putExtra(
                    ClearentSDKActivity.CLEARENT_SHOW_HINTS,
                    clearentAction.showHints
                )
            }
            is ClearentAction.DevicesList ->
                intent.putExtra(
                    ClearentSDKActivity.CLEARENT_ACTION_KEY,
                    ClearentSDKActivity.CLEARENT_ACTION_DEVICES
                )
            is ClearentAction.Settings ->
                intent.putExtra(
                    ClearentSDKActivity.CLEARENT_ACTION_KEY,
                    ClearentSDKActivity.CLEARENT_ACTION_SETTINGS
                )
            is ClearentAction.Transaction -> {
                intent.putExtra(
                    ClearentSDKActivity.CLEARENT_ACTION_KEY,
                    ClearentSDKActivity.CLEARENT_ACTION_TRANSACTION
                )
                intent.putExtra(
                    ClearentSDKActivity.CLEARENT_PAYMENT_INFO_KEY,
                    clearentAction.paymentInfo
                )
                intent.putExtra(
                    ClearentSDKActivity.CLEARENT_SHOW_HINTS,
                    clearentAction.showHints
                )
                intent.putExtra(
                    ClearentSDKActivity.CLEARENT_SHOW_SIGNATURE,
                    clearentAction.showSignature
                )
                intent.putExtra(
                    ClearentSDKActivity.CLEARENT_PAYMENT_METHOD,
                    clearentAction.paymentMethod as Parcelable
                )
            }
        }
        activityLauncher.launch(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        clearentWrapper.removeReaderStatusListener(this)
        clearentWrapper.removeOfflineModeEnabledListener(this)
    }

    private fun renderChargeAmount() {
        binding.apply {
            if (chargeAmount.isBlank()) {
                chargeButton.isEnabled = false
                chargeButton.text = getString(R.string.charge_amount, "")
            } else {
                chargeButton.isEnabled = true
                chargeButton.text = getString(R.string.charge_amount, formatChargeAmount())
            }

            chargeAmountText.text = formatChargeAmount()
        }
    }

    private fun formatChargeAmount(): String =
        chargeAmount.length.let { length ->
            when {
                length > 8 -> "$" + chargeAmount.let {
                    it.slice(IntRange(0, length - 9)) + "," +
                            it.slice(IntRange(length - 8, length - 6)) + "," +
                            it.slice(IntRange(length - 5, length - 3)) + "." +
                            it.takeLast(2)
                }
                length > 5 -> "$" + chargeAmount.let {
                    it.slice(IntRange(0, length - 6)) + "," +
                            it.slice(IntRange(length - 5, length - 3)) + "." +
                            it.takeLast(2)
                }
                length > 2 -> "$" + chargeAmount.insert(length - 2, ".")
                length == 2 -> "\$0.$chargeAmount"
                length == 1 -> "\$0.0$chargeAmount"
                else -> DEFAULT_CHARGE_AMOUNT
            }
        }

    private fun appendDigitToChargeAmount(digit: String) {
        if (chargeAmount.isBlank() && digit == "0")
            return

        if (chargeAmount.length >= 11)
            return

        chargeAmount = chargeAmount.plus(digit)
        renderChargeAmount()
    }

    private fun popDigitFromChargeAmount() {
        if (chargeAmount.isBlank())
            return

        chargeAmount = chargeAmount.dropLast(1)
        renderChargeAmount()
    }

    private fun clearChargeAmount() {
        chargeAmount = ""
        renderChargeAmount()
    }


    private fun setReaderState(readerState: ReaderState) {
        when (readerState) {
            is ReaderState.NoReader -> setNoReaderPaired()
            is ReaderState.ReaderUnavailable -> setReaderUnavailable(readerState)
            is ReaderState.ReaderPaired -> setReaderPaired(readerState)
        }
    }

    private fun setReaderPaired(readerState: ReaderState.ReaderPaired) {
        binding.apply {
            readerInfo.apply {
                devicePaired.visibility = View.VISIBLE

                noDeviceConnected.visibility = View.GONE
                deviceIdle.visibility = View.GONE

                readerState.apply {
                    devicesDropdown.text = reader.displayName
                    renderDeviceSignalStrength(signal)
                    renderDeviceBatteryLevel(battery)
                }
            }
        }
    }

    private fun renderDeviceSignalStrength(signalState: SignalState) {
        binding.apply {
            readerInfo.apply {
                setTextIcon(deviceSignalStrength, signalState.iconResourceId)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setOfflineModeEnabledText()
    }

    private fun renderDeviceBatteryLevel(batteryLifeState: BatteryLifeState) {
        binding.apply {
            readerInfo.apply {
                if (batteryLifeState.iconResourceId == 0) {
                    deviceBatteryLevel.isVisible = false
                    separator.isVisible = false
                } else {
                    deviceBatteryLevel.isVisible = true
                    separator.isVisible = true
                    deviceBatteryLevel.text =
                        getString(R.string.battery_life, batteryLifeState.batteryLevel)
                    setTextIcon(deviceBatteryLevel, batteryLifeState.iconResourceId)
                }
            }
        }
    }

    private fun setTextIcon(textView: TextView, iconId: Int) =
        textView.setCompoundDrawablesWithIntrinsicBounds(
            iconId,
            0,
            0,
            0
        )

    private fun setNoReaderPaired() {
        binding.apply {
            readerInfo.apply {
                devicesDropdown.text = getString(R.string.no_reader_message)

                noDeviceConnected.visibility = View.VISIBLE

                deviceIdle.visibility = View.GONE
                devicePaired.visibility = View.GONE
            }
        }
    }

    private fun setReaderUnavailable(readerState: ReaderState.ReaderUnavailable) {
        binding.apply {
            readerInfo.apply {
                devicesDropdown.text = readerState.reader.displayName

                deviceIdle.visibility = View.VISIBLE

                noDeviceConnected.visibility = View.GONE
                devicePaired.visibility = View.GONE

                deviceIdle.text = readerState.readerConnection.displayText
                setTextIcon(deviceIdle, readerState.readerConnection.iconResourceId)
            }
        }
    }

    private fun setOfflineModeEnabledText() {
        binding.apply {
            clearentWrapper.retrieveUnprocessedOfflineTransactions(onRetrieved = {
                offlineModeEnabled.text =
                    getString(R.string.offline_mode_enabled_text, it.size.toString())
            }, onError = {
                offlineModeEnabled.text = getString(R.string.offline_mode_enabled_text, " ")
            })
        }
    }

    override fun onReaderStatusUpdate(readerStatus: ReaderStatus?) {
        lifecycleScope.launch {
            renderCurrentReader(readerStatus)
        }
    }

    override fun onOfflineModeChanged(enabled: Boolean) {
        lifecycleScope.launch {
            binding.apply {
                offlineModeEnabled.visibility = if (enabled) View.VISIBLE else View.GONE
                setOfflineModeEnabledText()
            }
        }
    }
}