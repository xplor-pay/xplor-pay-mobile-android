package com.xplore.paymobile.ui.merchantselection.search.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.xplore.paymobile.databinding.ItemSeeMoreBinding

class SeeMoreListAdapter(val onItemClicked: (SeeMoreItem) -> Unit) :
    ListAdapter<SeeMoreListAdapter.SeeMoreItem, SeeMoreListAdapter.SeeMoreViewHolder>(
        SEE_MORE_COMPARATOR,
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeeMoreViewHolder {
        val binding =
            ItemSeeMoreBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SeeMoreViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SeeMoreViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    inner class SeeMoreViewHolder(private val binding: ItemSeeMoreBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {
                seeMoreButton.setOnClickListener {
                    val position = bindingAdapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val item = getItem(position)
                        onItemClicked.invoke(item)
                    }
                }
            }
        }

        fun bind(item: SeeMoreItem) {
            binding.apply {
                seeMoreButton.text = item.title
                seeMoreButton.isVisible = !item.isLoading
                progressBar.isVisible = item.isLoading
            }
        }
    }

    companion object {
        private val SEE_MORE_COMPARATOR = object : DiffUtil.ItemCallback<SeeMoreItem>() {
            override fun areItemsTheSame(oldItem: SeeMoreItem, newItem: SeeMoreItem) =
                oldItem.title == newItem.title && oldItem.isLoading == newItem.isLoading

            override fun areContentsTheSame(oldItem: SeeMoreItem, newItem: SeeMoreItem) =
                oldItem == newItem
        }
    }

    data class SeeMoreItem(
        val title: String,
        var isLoading: Boolean,
    )
}
