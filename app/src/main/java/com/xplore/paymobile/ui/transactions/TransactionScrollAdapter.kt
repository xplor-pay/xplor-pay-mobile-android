//package com.xplore.paymobile.ui.transactions
//
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import androidx.recyclerview.widget.RecyclerView
//import com.xplore.paymobile.databinding.FragmentTransactionsBinding
//
//abstract class TransactionScrollAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
//
//    private var currentScrollPosition = 0
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//        val scrollBinding = FragmentTransactionsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//    }
//
//    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//        currentScrollPosition = holder.bindingAdapterPosition
//    }
//
//}