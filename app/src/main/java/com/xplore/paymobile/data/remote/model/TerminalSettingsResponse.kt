package com.xplore.paymobile.data.remote.model

import com.google.gson.annotations.SerializedName

data class TerminalSettingsResponse(
    @SerializedName("code") var code: String,
    @SerializedName("status") var status: String,
    var payload: TerminalSettingsPayloadResponse
)

data class TerminalSettingsPayloadResponse(
    val payloadType: String? = null,
    @SerializedName("terminal-settings")
    val terminalSettings: TerminalSettings
)

data class TerminalSettings(
    @SerializedName("time-zone") val timeZone: String = ""
)
