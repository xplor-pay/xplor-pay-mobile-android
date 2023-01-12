package com.xplore.paymobile.ui.merchantselection.search.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.xplore.paymobile.R
import com.xplore.paymobile.databinding.ItemMerchantListBinding

class MerchantsListAdapter(val onItemClicked: (MerchantItem, Int) -> Unit) :
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
                        handleSelection(item, position)
                        onItemClicked.invoke(item, position)
                    }
                }
            }
        }

        fun bind(item: MerchantItem) {
            binding.apply {
                merchantTextView.text = item.name
                when (item.isSelected) {
                    true -> {
                        root.setBackgroundColor(root.context.getColor(R.color.gray))
                    }
                    false -> {
                        root.setBackgroundColor(root.context.getColor(R.color.white))
                    }
                }
            }
        }

        private fun handleSelection(
            item: MerchantItem,
            position: Int
        ) {
            if (item.isSelected) {
                item.isSelected = false
                notifyItemChanged(position)
                return
            }
            currentList.mapIndexed { index, listItem ->
                if (item == listItem) {
                    listItem.isSelected = item == listItem
                    notifyItemChanged(index)
                } else if (listItem.isSelected) {
                    listItem.isSelected = false
                    notifyItemChanged(index)
                }
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
        val name: String,
        val id: String,
        var isSelected: Boolean = false
    )
}