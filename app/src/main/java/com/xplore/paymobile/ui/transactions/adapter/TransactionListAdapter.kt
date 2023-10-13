package com.xplore.paymobile.ui.transactions.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.xplore.paymobile.R
import com.xplore.paymobile.databinding.TransactionCardBinding
import com.xplore.paymobile.ui.transactions.util.DateFormatUtil

class TransactionListAdapter(val onItemClicked: (TransactionItem, Int) -> Unit) :
    ListAdapter<TransactionListAdapter.TransactionItem, TransactionListAdapter.TransactionViewHolder>(
        TRANSACTION_COMPARATOR
    ) {

    private var currentScrollPosition = 0
    private val refund = "Refund"
    private val void = "Void"
    private val sale = "Sale"
    private val forcedSale = "Forced Sale"
    private val auth = "Auth"
    private val unmatchedRefund = "Unmatched Refund"
    private val approved = "Approved"
    private val declined = "Declined"
    private val settled = "Settled"
    private val error = "System Error"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding =
            TransactionCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        println("position: $position")
        currentScrollPosition = holder.bindingAdapterPosition
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    fun getCurrentScrollPosition(): Int {
        return currentScrollPosition
    }

    inner class TransactionViewHolder(private val binding: TransactionCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.apply {
                root.setOnClickListener {
                    val position = bindingAdapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val item = getItem(position)
                        onItemClicked.invoke(item, position)
                    }
                }
            }
        }

        fun bind(transactionItem: TransactionItem) {
            binding.created.text = DateFormatUtil.formatDateTime(transactionItem.created)
            binding.amount.text = buildString {
                append("$")
                append(transactionItem.amount)
            }

            bindTransactionTypeAndStatusFields(transactionItem)

            binding.maskedCard.text = buildString {
                append("**** ")
                append(transactionItem.card)
            }
        }

        private fun bindTransactionTypeAndStatusFields(transactionItem: TransactionItem) {
            if (transactionItem.settled) {
                bindSettledTransactionFields()
            } else if (transactionItem.type == "VOID") {
                bindVoidTransactionFields()
            } else {
                bindTransactionType(transactionItem.type)
                bindTransactionStatus(transactionItem.status)
            }
        }

        private fun bindSettledTransactionFields() {
            binding.type.text = sale
            binding.type.setBackgroundResource(R.drawable.bg_pill_purple)
            binding.status.text = settled
            binding.status.setBackgroundResource(R.drawable.bg_pill_purple)
        }

        private fun bindVoidTransactionFields() {
            binding.type.text = sale
            binding.type.setBackgroundResource(R.drawable.bg_pill_purple)
            binding.status.text = void
            binding.status.setBackgroundResource(R.drawable.bg_pill_pink)
        }

        //todo could use one pill background and change the color per type or status
        private fun bindTransactionType(type: String) {
            when (type) {
                "SALE" -> {
                    binding.type.text = sale
                    binding.type.setBackgroundResource(R.drawable.bg_pill_purple)
                }
                "FORCED SALE" -> {
                    binding.type.text = forcedSale
                    binding.type.setBackgroundResource(R.drawable.bg_pill_blue)
                }
                "VOID" -> {
                    binding.type.text = void
                    binding.type.setBackgroundResource(R.drawable.bg_pill_pink)
                }
                "REFUND" -> {

                    binding.type.text = refund
                    binding.type.setBackgroundResource(R.drawable.bg_pill_yellow)
                }
                "UNMATCHED REFUND" -> {
                    binding.type.text = unmatchedRefund
                    binding.type.setBackgroundResource(R.drawable.bg_pill_pink)
                }
                "AUTH" -> {
                    binding.type.text = auth
                    binding.type.setBackgroundResource(R.drawable.bg_pill_pink)
                }

            }
        }

        private fun bindTransactionStatus(status: String) {
            when (status) {
                "Approved" -> {
                    binding.status.text = approved
                    binding.status.setBackgroundResource(R.drawable.bg_pill_green)
                }
                "Declined" -> {
                    binding.status.text = declined
                    binding.status.setBackgroundResource(R.drawable.bg_pill_pink)
                }
                else -> {
                    binding.status.text = error
                    binding.status.setBackgroundResource(R.drawable.bg_pill_pink)
                }

            }
        }
    }

    companion object {
        private val TRANSACTION_COMPARATOR = object : DiffUtil.ItemCallback<TransactionItem>() {
            override fun areItemsTheSame(oldItem: TransactionItem, newItem: TransactionItem) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: TransactionItem, newItem: TransactionItem) =
                oldItem == newItem
        }
    }

    data class TransactionItem(
        val id: String,
        val amount: String,
        val created: String,
        val type: String,
        val status: String,
        val card: String,
        val settled: Boolean
//        val pending: Boolean
    )
}