package com.xplore.paymobile.ui.transactions.model

data class TransactionItem(
    val id: String,
    val amount: String,
    val created: String,
    val type: String,
    val status: String,
    val card: String,
    val settled: Boolean,
    val pending: Boolean
)