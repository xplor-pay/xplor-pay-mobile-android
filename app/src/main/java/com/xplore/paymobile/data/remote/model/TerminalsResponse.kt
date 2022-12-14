package com.xplore.paymobile.data.remote.model

import com.google.gson.annotations.SerializedName

class TerminalsResponse : ArrayList<Terminal>()

data class Terminal(
    @SerializedName("questJwt") val questJwt: QuestJwt,
    @SerializedName("selected") val selected: Boolean,
    @SerializedName("terminalName") val terminalName: String,
    @SerializedName("terminalPKId") val terminalPKId: String
)

data class QuestJwt(
    @SerializedName("subject") val subject: String,
    @SerializedName("terminalId") val terminalId: String,
    @SerializedName("token") val token: String
)