package com.xplore.paymobile

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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
import com.clearent.idtech.android.wrapper.ui.util.checkPermissionsToRequest
import com.google.android.gms.common.util.DeviceProperties
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

    private val clearentWrapper = ClearentWrapper.getInstance()

    private var hintsShowed = false
    private lateinit var binding: ActivityMainBinding

    private val multiplePermissionsContract = ActivityResultContracts.RequestMultiplePermissions()
    private val multiplePermissionsLauncher =
        registerForActivityResult(multiplePermissionsContract) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = if (DeviceProperties.isTablet(resources))
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        installSplashScreen()

        super.onCreate(savedInstanceState)

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
        clearentWrapper.setListener(ClearentDataSource)
    }

    override fun onDestroy() {
        super.onDestroy()
        clearentWrapper.removeListener()
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