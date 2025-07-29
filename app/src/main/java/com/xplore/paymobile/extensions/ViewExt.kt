package com.xplore.paymobile.extensions

import android.view.View
import androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

fun View.handleEdgeToEdge() {
    setOnApplyWindowInsetsListener(this) { view, windowInsets ->
        val systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
        val imeVisible = windowInsets.isVisible(WindowInsetsCompat.Type.ime())
        view.updatePadding(
            left = systemBars.left,
            top = systemBars.top,
            right = systemBars.right,
            bottom = if (imeVisible) windowInsets.getInsets(WindowInsetsCompat.Type.ime()).bottom else systemBars.bottom,
        )
        WindowInsetsCompat.CONSUMED
    }
}
