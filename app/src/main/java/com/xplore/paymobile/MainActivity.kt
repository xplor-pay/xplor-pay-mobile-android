package com.xplore.paymobile

import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import androidx.fragment.app.commit
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
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.xplore.paymobile.data.datasource.RemoteDataSource
import com.xplore.paymobile.data.datasource.SharedPreferencesDataSource
import com.xplore.paymobile.databinding.ActivityMainBinding
import com.xplore.paymobile.ui.FirstPairListener
import com.xplore.paymobile.ui.login.LoginFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), FirstPairListener {

    @Inject
    lateinit var rds: RemoteDataSource
    @Inject
    lateinit var spds: SharedPreferencesDataSource

    companion object {
        private const val HINTS_DISPLAY_DELAY = 3000L
    }

    private var hintsShowed = false
    private var showBottomNav = true

    private lateinit var binding: ActivityMainBinding

    private val multiplePermissionsContract = ActivityResultContracts.RequestMultiplePermissions()
    private val multiplePermissionsLauncher =
        registerForActivityResult(multiplePermissionsContract) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // TODO: remove this, used to remove the auth token since there is no logout
        spds.setAuthToken(null)

        setupListener()

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
    }

    private fun setupViews() {
        setupWebViewLogin()
        setupAppView()
    }

    private fun setupWebViewLogin() {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(
                R.id.login_fragment,
                LoginFragment {
                    //TODO Provisional, clearing merchant and terminal should be moved to logout method
                    spds.clearMerchant()
                    spds.clearTerminal()
                    binding.container.isVisible = true
                    binding.loginFragment.isVisible = false
                }
            )
        }
    }

    private fun setupAppView() {
        val navView: BottomNavigationView = binding.navView
        navView.isVisible = showBottomNav

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_payment,
                R.id.navigation_transactions,
                R.id.navigation_batches,
                R.id.navigation_settings,
                R.id.navigation_info
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

    fun showBottomNavigation(show: Boolean) {
        showBottomNav = show
        findViewById<BottomNavigationView>(R.id.nav_view)?.isVisible = show
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
