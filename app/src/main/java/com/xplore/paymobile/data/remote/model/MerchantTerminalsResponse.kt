package com.xplore.paymobile.data.remote.model

import com.google.gson.annotations.SerializedName

class MerchantTerminalsResponse : ArrayList<MerchantTerminal>()

data class MerchantTerminal(
    @SerializedName("merchantTerminalId") val merchantTerminalId: String,
    @SerializedName("terminalName") val terminalName: String
)