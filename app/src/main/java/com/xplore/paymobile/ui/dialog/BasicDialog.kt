package com.xplore.paymobile.ui.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.xplore.paymobile.R

class BasicDialog(private val title: String, private val message: String) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        builder.apply {
            setTitle(title)
            setMessage(message)
            setPositiveButton(R.string.ok) { _, _ ->
                dismiss()
            }
        }
        return builder.create()
    }
}