package com.xplore.paymobile.ui.merchantselection.search.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.xplore.paymobile.databinding.ItemMerchantListBinding

class MerchantsListAdapter(val onItemClicked: (MerchantItem) -> Unit) :
    ListAdapter<MerchantsListAdapter.MerchantItem, MerchantsListAdapter.MerchantViewHolder>(
        MERCHANT_COMPARATOR
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MerchantViewHolder {
        val binding =
            ItemMerchantListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MerchantViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MerchantViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    inner class MerchantViewHolder(private val binding: ItemMerchantListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {
                root.setOnClickListener {
                    val position = bindingAdapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val item = getItem(position)
                        onItemClicked.invoke(item)
                    }
                }
            }
        }

        fun bind(item: MerchantItem) {
            binding.apply {
                merchantTextView.text = item.name
            }
        }
    }

    companion object {
        private val MERCHANT_COMPARATOR = object : DiffUtil.ItemCallback<MerchantItem>() {
            override fun areItemsTheSame(oldItem: MerchantItem, newItem: MerchantItem) =
                oldItem.name == newItem.name

            override fun areContentsTheSame(oldItem: MerchantItem, newItem: MerchantItem) =
                oldItem == newItem
        }
    }

    data class MerchantItem(
        val name: String
    )
}