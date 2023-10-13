package com.xplore.paymobile.data.remote.model

import com.google.gson.annotations.SerializedName

data class TerminalSettingsResponse(
    @SerializedName("code") var code: String,
    @SerializedName("status") var status: String,
    @SerializedName("exchange-id") var exchangeId: String,
    var payload: TerminalSettingsPayload
)

data class TerminalSettingsPayload(
    var error: ResponseError,
    var transactions: TerminalSettings?,
)

data class TerminalSettings(
    @SerializedName("terminal-settings") var terminalSettings: TerminalSetting
)

data class TerminalSetting(
    @SerializedName("time-zone") var timeZone: String
)