package com.xplore.paymobile

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.clearent.idtech.android.wrapper.ClearentDataSource
import com.clearent.idtech.android.wrapper.SDKWrapper
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.xplore.paymobile.databinding.ActivityMainBinding
import com.xplore.paymobile.ui.FirstPairListener
import com.xplore.paymobile.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), FirstPairListener {

    companion object {
        private const val HINTS_DISPLAY_DELAY = 3000L
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initSdkWrapper()

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupHints()

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
    }

    private fun initSdkWrapper() {
        SDKWrapper.initializeReader(
            applicationContext,
            Constants.BASE_URL_SANDBOX,
            Constants.PUBLIC_KEY_SANDBOX,
            Constants.API_KEY_SANDBOX
        )
        SDKWrapper.setListener(ClearentDataSource)
    }

    private fun setupHints() {
        binding.hints.apply {
            root.visibility = View.GONE

            hintsPairingTip.hintsTipText.text = getString(R.string.first_pairing_tip_text)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        SDKWrapper.removeListener()
    }

    override fun showFirstPair(onClick: () -> Unit, onDismiss: () -> Unit) {
        lifecycleScope.launch {
            binding.hints.apply {
                root.bringToFront()
                hintsFirstReaderButton.setOnClickListener {
                    root.visibility = View.GONE
                    onClick()
                }

                hintsSkipPairing.setOnClickListener {
                    root.visibility = View.GONE
                    onDismiss()
                }
                renderHints()
            }
        }
    }

    private fun renderHints() = lifecycleScope.launch {
        delay(HINTS_DISPLAY_DELAY)
        binding.hints.root.visibility = View.VISIBLE
    }
}
