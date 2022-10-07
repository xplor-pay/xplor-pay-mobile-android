package com.xplore.paymobile

import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.clearent.idtech.android.wrapper.ClearentDataSource
import com.clearent.idtech.android.wrapper.ClearentWrapper
import com.clearent.idtech.android.wrapper.model.StoreAndForwardMode
import com.clearent.idtech.android.wrapper.ui.util.checkPermissionsToRequest
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.xplore.paymobile.databinding.ActivityMainBinding
import com.xplore.paymobile.ui.FirstPairListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), FirstPairListener {

    companion object {
        private const val HINTS_DISPLAY_DELAY = 3000L
    }

    private var hintsShowed = false
    private lateinit var binding: ActivityMainBinding

    private val multiplePermissionsContract = ActivityResultContracts.RequestMultiplePermissions()
    private val multiplePermissionsLauncher =
        registerForActivityResult(multiplePermissionsContract) {}

    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            // Is the button now checked?
            val checked = view.isChecked

            // Check which radio button was clicked
            when (view.getId()) {
                R.id.radio_button_1 ->
                    if (checked) {
                        ClearentWrapper.storeAndForwardMode = StoreAndForwardMode.ON
                    }
                R.id.radio_button_2 ->
                    if (checked) {
                        ClearentWrapper.storeAndForwardMode = StoreAndForwardMode.PROMPT
                    }
                R.id.radio_button_3 ->
                    if (checked) {
                        ClearentWrapper.storeAndForwardMode = StoreAndForwardMode.OFF
                    }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ClearentWrapper.registerNetworkListener()

        setupListener()

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_transactions, R.id.navigation_more
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        supportActionBar?.hide()

        askPermissions()
    }

    private fun askPermissions() =
        multiplePermissionsLauncher.launch(checkPermissionsToRequest(context = applicationContext))

    private fun setupListener() {
        ClearentWrapper.setListener(ClearentDataSource)
    }

    override fun onDestroy() {
        super.onDestroy()
        ClearentWrapper.removeListener()

        ClearentWrapper.unregisterNetworkCallback()
    }

    override fun showFirstPair(onClick: () -> Unit, onDismiss: () -> Unit) {
        if (hintsShowed)
            return

        hintsShowed = true
        lifecycle.addObserver(
            ListenerCallbackObserver(lifecycle) {
                lifecycleScope.launch {
                    binding.apply {
                        hintsContainer.visibility = View.VISIBLE
                        hintsContainer.bringToFront()

                        hints.apply {
                            root.visibility = View.GONE

                            hintsPairingTip.hintsTipText.text =
                                getString(R.string.first_pairing_tip_text)
                            hintsFirstReaderButton.setOnClickListener {
                                hintsContainer.visibility = View.GONE
                                onClick()
                            }
                            hintsSkipPairing.setOnClickListener {
                                hintsContainer.visibility = View.GONE
                                onDismiss()
                            }

                            renderHints()
                        }
                    }
                }
            }
        )
    }

    private fun renderHints() = lifecycleScope.launch {
        delay(HINTS_DISPLAY_DELAY)
        binding.hints.root.visibility = View.VISIBLE
    }

    class ListenerCallbackObserver(
        private val lifecycle: Lifecycle,
        private val callback: () -> Unit
    ) : DefaultLifecycleObserver {

        override fun onStart(owner: LifecycleOwner) {
            callback()
            lifecycle.removeObserver(this)
        }
    }
}
