package com.xplore.paymobile.model

import com.clearent.idtech.android.R

sealed class ReaderState {
    object NoReader : ReaderState()
    data class ReaderIdle(val reader: Reader) : ReaderState()
    data class ReaderPaired(
        val reader: Reader,
        val status: SignalState,
        val battery: BatteryLifeState
    ) : ReaderState()
}

data class Reader(
    val id: String,
    val name: String,
)

data class BatteryLifeState(
    val batteryLevel: Int = 0
) {

    val iconResourceId = when {
        batteryLevel > 75 -> R.drawable.ic_battery_life_100
        batteryLevel > 50 -> R.drawable.ic_battery_life_75
        batteryLevel > 45 -> R.drawable.ic_battery_life_50
        batteryLevel > 25 -> R.drawable.ic_battery_life_45
        batteryLevel > 10 -> R.drawable.ic_battery_life_25
        else -> R.drawable.ic_battery_life_10
    }
}

enum class SignalState(
    val iconResourceId: Int
) {

    NoSignal(
        iconResourceId = R.drawable.ic_no_signal
    ),
    Weak(
        iconResourceId = R.drawable.ic_signal_weak
    ),
    Medium(
        iconResourceId = R.drawable.ic_signal_medium
    ),
    Good(
        iconResourceId = R.drawable.ic_signal_good
    );
}
