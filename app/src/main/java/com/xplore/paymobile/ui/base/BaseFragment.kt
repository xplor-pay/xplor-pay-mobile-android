package com.xplore.paymobile.ui.base

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.xplore.paymobile.MainActivity

open class BaseFragment: Fragment() {

    open val hasBottomNavigation = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as? MainActivity)?.showBottomNavigation(hasBottomNavigation)
    }
}