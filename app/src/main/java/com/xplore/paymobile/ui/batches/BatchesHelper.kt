//package com.xplore.paymobile.ui.batches
//
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import androidx.recyclerview.widget.DiffUtil
//import androidx.recyclerview.widget.ListAdapter
//import androidx.recyclerview.widget.RecyclerView
//import com.xplore.paymobile.R
//import com.xplore.paymobile.databinding.BatchCardBinding
//import com.xplore.paymobile.databinding.TransactionCardBinding
//
//class BatchesAdapter() :
//    ListAdapter<BatchesAdapter.BatchItem, BatchesAdapter.BatchesViewHolder>(
//        BATCH_COMPARATOR
//    ) {
//
//    private var currentScrollPosition = 0
//    private val refund = "Refund"
//    private val void = "Void"
//    private val sale = "Sale"
//    private val forcedSale = "Forced Sale"
//    private val auth = "Auth"
//    private val unmatchedRefund = "Unmatched Refund"
//    private val approved = "Approved"
//    private val declined = "Declined"
//    private val settled = "Settled"
//    private val error = "System Error"
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BatchesViewHolder {
//        val binding =
//            BatchCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        return BatchesViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: BatchesViewHolder, position: Int) {
////        println("position: $position")
////        currentScrollPosition = holder.bindingAdapterPosition
////        val currentItem = getItem(position)
////        holder.bind(currentItem)
//    }
//
//    fun getCurrentScrollPosition(): Int {
//        return currentScrollPosition
//    }
//
//    inner class BatchesViewHolder(private val binding: BatchCardBinding) :
//        RecyclerView.ViewHolder(binding.root) {
//
//        fun bind(batchItem: BatchItem) {
//            binding.batchNumber.text = batchItem.created
//            binding.amount.text = buildString {
//                append("$")
//                append(batchItem.amount)
//            }
//
//
//        }
//    }
//
//    companion object {
//        private val BATCH_COMPARATOR = object : DiffUtil.ItemCallback<BatchItem>() {
//            override fun areItemsTheSame(oldItem: BatchItem, newItem: BatchItem) =
//                oldItem.id == newItem.id
//
//            override fun areContentsTheSame(oldItem: BatchItem, newItem: BatchItem) =
//                oldItem == newItem
//        }
//    }
//
//    data class BatchItem(
//        val id: String,
//        val amount: String,
//        val created: String?,
//        val type: String,
//        val status: String,
//        val card: String?,
//        val settled: Boolean,
//        val pending: Boolean
//    )
//}