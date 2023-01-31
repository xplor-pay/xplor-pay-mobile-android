package com.xplore.paymobile.ui.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.xplore.paymobile.R

class BasicDialog(
    private val title: String,
    private val message: String,
    private val onPositiveButtonClick: () -> Unit = {},
    private val onDismiss: (() -> Unit)? = null
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        builder.apply {
            setTitle(title)
            setMessage(message)
            setPositiveButton(R.string.ok) { _, _ ->
                dismiss()
                onPositiveButtonClick()
            }
        }
        return builder.create()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismiss?.invoke()
    }
}