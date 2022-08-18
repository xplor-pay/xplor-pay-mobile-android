package com.xplore.paymobile.ui.home

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
import com.clearent.idtech.android.wrapper.SDKWrapper
import com.clearent.idtech.android.wrapper.listener.ReaderStatusListener
import com.clearent.idtech.android.wrapper.model.BatteryLifeState
import com.clearent.idtech.android.wrapper.model.ReaderState
import com.clearent.idtech.android.wrapper.model.ReaderStatus
import com.clearent.idtech.android.wrapper.model.SignalState
import com.clearent.idtech.android.wrapper.ui.MainActivity
import com.clearent.idtech.android.wrapper.ui.PaymentMethod
import com.clearent.idtech.android.wrapper.ui.SDKWrapperAction
import com.xplore.paymobile.R
import com.xplore.paymobile.databinding.FragmentHomeBinding
import com.xplore.paymobile.ui.FirstPairListener
import com.xplore.paymobile.util.SharedPreferencesDataSource.FirstPair
import com.xplore.paymobile.util.insert
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class HomeFragment : Fragment(), ReaderStatusListener {

    companion object {
        private const val DEFAULT_CHARGE_AMOUNT = "$0.00"
    }

    private val viewModel by viewModels<HomeViewModel>()

    private var chargeAmount = ""
    private var transactionOngoing = false
    private var shouldShowSignature = true

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val activityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Timber.d(result.resultCode.toString())
        transactionOngoing = false
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
        SDKWrapper.addReaderStatusListener(this)
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
                openDevicesList()
            }
            firstReader.setOnClickListener {
                startPairingProcess()
            }
            chargeButton.setOnClickListener {
                startSdkActivityForResult(
                    SDKWrapperAction.Transaction(
                        chargeAmount.toDouble() / 100,
                        viewModel.shouldShowHints()
                    )
                )
            }
        }
    }

    private fun startPairingProcess() =
        startSdkActivityForResult(SDKWrapperAction.Pairing(viewModel.shouldShowHints()))

    private fun openDevicesList() =
        startSdkActivityForResult(SDKWrapperAction.DevicesList)

    private fun startSdkActivityForResult(sdkWrapperAction: SDKWrapperAction) {
        if (transactionOngoing)
            return

        transactionOngoing = true

        val intent = Intent(requireContext(), MainActivity::class.java)
        when (sdkWrapperAction) {
            is SDKWrapperAction.Pairing -> {
                intent.putExtra(
                    MainActivity.SDK_WRAPPER_ACTION_KEY,
                    MainActivity.SDK_WRAPPER_ACTION_PAIR
                )
                intent.putExtra(
                    MainActivity.SDK_WRAPPER_SHOW_HINTS,
                    sdkWrapperAction.showHints
                )
            }
            is SDKWrapperAction.DevicesList ->
                intent.putExtra(
                    MainActivity.SDK_WRAPPER_ACTION_KEY,
                    MainActivity.SDK_WRAPPER_ACTION_DEVICES
                )
            is SDKWrapperAction.Transaction -> {
                intent.putExtra(
                    MainActivity.SDK_WRAPPER_ACTION_KEY,
                    MainActivity.SDK_WRAPPER_ACTION_TRANSACTION
                )
                intent.putExtra(MainActivity.SDK_WRAPPER_AMOUNT_KEY, sdkWrapperAction.amount)
                intent.putExtra(
                    MainActivity.SDK_WRAPPER_SHOW_HINTS,
                    sdkWrapperAction.showHints
                )
                intent.putExtra(MainActivity.SDK_WRAPPER_SHOW_SIGNATURE, shouldShowSignature)
                val paymentMethod =
                    if (viewModel.isCardReaderSelected) PaymentMethod.CARD_READER else PaymentMethod.MANUAL_ENTRY
                intent.putExtra(MainActivity.SDK_WRAPPER_PAYMENT_METHOD, paymentMethod as Parcelable)
            }
        }
        activityLauncher.launch(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        SDKWrapper.removeReaderStatusListener(this)
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

    override fun onReaderStatusUpdate(readerStatus: ReaderStatus?) {
        lifecycleScope.launch {
            renderCurrentReader(readerStatus)
        }
    }
}