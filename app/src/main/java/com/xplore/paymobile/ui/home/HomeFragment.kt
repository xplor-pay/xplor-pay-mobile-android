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
import com.xplore.paymobile.R
import com.xplore.paymobile.databinding.FragmentHomeBinding
import com.xplore.paymobile.model.BatteryLifeState
import com.xplore.paymobile.model.Reader
import com.xplore.paymobile.model.ReaderState
import com.xplore.paymobile.model.SignalState
import com.xplore.paymobile.util.insert
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

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

        renderChargeAmount()

        binding.chargeButton.setOnClickListener {
            viewModel.cycleReaders()
        }

        return binding.root
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
            chargeCounter.text = formatChargeAmount()
            chargeButton.text = getString(R.string.charge_amount, formatChargeAmount())
        }
    }

    private fun formatChargeAmount(): String = if (chargeAmount.isBlank()) {
        defaultChargeAmount
    } else {
        "\$$chargeAmount".let {
            it.insert(it.length - 2, ".")
        }
    }

    private fun appendDigitToChargeAmount(digit: String) {
        if (chargeAmount.isBlank() && digit == "0")
            return

        chargeAmount.plus(digit)
        renderChargeAmount()
    }

    private fun popDigitFromChargeAmount() {
        if (chargeAmount.isBlank())
            return

        chargeAmount.dropLast(1)
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

    private fun renderDeviceSignalStrength(signalState: SignalState) {
        binding.apply {
            setTextIcon(deviceSignalStrength, signalState.iconResourceId)
        }
    }

    private fun renderDeviceBatteryLevel(batteryLifeState: BatteryLifeState) {
        binding.apply {
            deviceBatteryLevel.text =
                getString(R.string.battery_life, batteryLifeState.batteryLevel)
            setTextIcon(deviceBatteryLevel, batteryLifeState.iconResourceId)
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
            devicesDropdown.text = getString(R.string.no_reader_message)

            noDeviceConnected.visibility = View.VISIBLE

            deviceIdle.visibility = View.GONE
            devicePaired.visibility = View.GONE
        }
    }

    private fun setReaderIdle(reader: Reader) {
        binding.apply {
            devicesDropdown.text = reader.name

            deviceIdle.visibility = View.VISIBLE

            noDeviceConnected.visibility = View.GONE
            devicePaired.visibility = View.GONE
        }
    }
}