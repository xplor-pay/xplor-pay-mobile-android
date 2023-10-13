//package com.xplore.paymobile.ui.transactions.dialog
//
//import android.widget.Button
//import android.widget.TextView
//import androidx.core.content.ContentProviderCompat.requireContext
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.google.android.material.bottomsheet.BottomSheetDialog
//import com.xplore.paymobile.R
//import com.xplore.paymobile.ui.transactions.adapter.TransactionListAdapter
//import com.xplore.paymobile.ui.transactions.util.DateFormatUtil
//
//class ProcessTransactionDialog {
//    private var transactionLinearLayout: LinearLayoutManager? = null
//    private fun showProcessTransactionDialog(
//        transactionItem: TransactionListAdapter.TransactionItem,
//        transactionType: String
//    ) {
//        val processTransactionDialog = BottomSheetDialog(requireContext())
//        processTransactionDialog.setContentView(R.layout.process_transaction)
//
//        transactionLinearLayout = LinearLayoutManager(requireContext())
//
//        val cancelButton = processTransactionDialog.findViewById<Button>(R.id.cancel_button)
//        val processTransactionButton =
//            processTransactionDialog.findViewById<Button>(R.id.process_transaction_button)
//        processTransactionButton?.text = transactionType
//
//        processTransactionDialog.findViewById<TextView>(R.id.amount)?.text =
//            buildString {
//                append("$")
//                append(transactionItem.amount)
//            }
//        processTransactionDialog.findViewById<TextView>(R.id.created)?.text =
//            DateFormatUtil.formatDateTime(transactionItem.created)
//
//        cancelButton?.setOnClickListener {
//            processTransactionDialog.dismiss()
//        }
//
//        processTransactionButton?.setOnClickListener {
//            processTransaction(transactionItem)
//        }
//
//        processTransactionDialog.setCanceledOnTouchOutside(true)
//        processTransactionDialog.setCancelable(true)
//
//        processTransactionDialog.show()
//    }
//
//    //todo override the onClick and process transaction in Fragment
//    private fun processTransaction(transactionItem: TransactionListAdapter.TransactionItem) {
//        println("here is id $id")
//        viewModel.processTransaction(transactionItem)
//    }
//}