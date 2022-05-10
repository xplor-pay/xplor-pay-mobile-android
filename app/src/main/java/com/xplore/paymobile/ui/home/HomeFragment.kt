package com.xplore.paymobile.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.clearent.idtech.android.wrapper.SDKWrapper
import com.clearent.idtech.android.wrapper.ui.TransactionBottomSheetFragment
import com.xplore.paymobile.R
import com.xplore.paymobile.databinding.FragmentHomeBinding
import com.xplore.paymobile.model.BatteryLifeState
import com.xplore.paymobile.model.Reader
import com.xplore.paymobile.model.ReaderState
import com.xplore.paymobile.model.SignalState
import com.xplore.paymobile.util.Constants
import com.xplore.paymobile.util.insert
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class HomeFragment : Fragment() {

    companion object {
        private const val defaultChargeAmount = "$0.00"
    }

    private val viewModel by viewModels<HomeViewModel>()

    private var chargeAmount = ""

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        setNumericKeyPadBackground()

        renderChargeAmount()

        setListeners()

        setUpNumpad()

        SDKWrapper.initializeReader(
            requireContext(),
            Constants.BASE_URL_SANDBOX,
            Constants.PUBLIC_KEY_SANDBOX
        )

        SDKWrapper.setMockDevice("IDTECH-VP3300-26863")

        return binding.root
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
            readerInfo.devicesDropdown.setOnClickListener {
                viewModel.cycleReaders()
            }
            chargeButton.setOnClickListener {
                Timber.e("BUTTTOOOON")
                val modalBottomSheet = TransactionBottomSheetFragment()
                modalBottomSheet.show(parentFragmentManager, TransactionBottomSheetFragment.TAG)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
    }

    private fun setupViewModel() {
        // Start a coroutine in the lifecycle scope
        viewLifecycleOwner.lifecycleScope.launch {
            // repeatOnLifecycle launches the block in a new coroutine every time the
            // lifecycle is in the STARTED state (or above) and cancels it when it's STOPPED.
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.readerState.collect { readerState ->
                    setReaderState(readerState)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
                else -> defaultChargeAmount
            }
        }

    private fun appendDigitToChargeAmount(digit: String) {
        if (chargeAmount.isBlank() && digit == "0")
            return

        if (chargeAmount.length >= 10)
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
            is ReaderState.ReaderIdle -> setReaderIdle(readerState.reader)
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
                    devicesDropdown.text = reader.name
                    renderDeviceSignalStrength(status)
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
                deviceBatteryLevel.text =
                    getString(R.string.battery_life, batteryLifeState.batteryLevel)
                setTextIcon(deviceBatteryLevel, batteryLifeState.iconResourceId)
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

    private fun setReaderIdle(reader: Reader) {
        binding.apply {
            readerInfo.apply {
                devicesDropdown.text = reader.name

                deviceIdle.visibility = View.VISIBLE

                noDeviceConnected.visibility = View.GONE
                devicePaired.visibility = View.GONE
            }
        }
    }
}