package com.xplore.paymobile.ui.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class BasicDialog(
    private val title: String,
    private val message: String?,
    private val positiveButton: DialogButton? = null,
    private val negativeButton: DialogButton? = null,
    private val onDismiss: (() -> Unit)? = null,
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        builder.apply {
            setTitle(title)
            setMessage(message)
            positiveButton?.also {
                setPositiveButton(positiveButton.text) { _, _ ->
                    positiveButton.onClick()
                    dismiss()
                }
            }
            negativeButton?.also {
                setNegativeButton(it.text) { _, _ ->
                    it.onClick()
                    dismiss()
                }
            }
        }
        return builder.create()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismiss?.invoke()
    }

    data class DialogButton(val text: String, val onClick: () -> Unit)
}
