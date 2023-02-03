package com.xplore.paymobile

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.lifecycle.*
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.clearent.idtech.android.wrapper.ClearentDataSource
import com.clearent.idtech.android.wrapper.ClearentWrapper
import com.clearent.idtech.android.wrapper.ui.util.checkPermissionsToRequest
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView.OnItemSelectedListener
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.xplore.paymobile.data.datasource.SharedPreferencesDataSource
import com.xplore.paymobile.data.web.LoginEvents
import com.xplore.paymobile.data.web.WebEventsSharedViewModel
import com.xplore.paymobile.databinding.ActivityMainBinding
import com.xplore.paymobile.interactiondetection.UserInteractionDetector
import com.xplore.paymobile.interactiondetection.UserInteractionEvent
import com.xplore.paymobile.ui.FirstPairListener
import com.xplore.paymobile.ui.dialog.BasicDialog
import com.xplore.paymobile.ui.login.LoginFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), FirstPairListener {

    private val webViewModel by viewModels<WebEventsSharedViewModel>()
    private val viewModel by viewModels<MainViewModel>()

    @Inject
    lateinit var sharedPreferencesDataSource: SharedPreferencesDataSource

    @Inject
    lateinit var interactionDetector: UserInteractionDetector

    companion object {
        private const val HINTS_DISPLAY_DELAY = 3000L
    }

    private val clearentWrapper = ClearentWrapper.getInstance()

    private var hintsShowed = false
    private var showBottomNav = true
    private var bottomNavItemIdSelected = R.id.navigation_payment

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    private lateinit var appUpdateManager: AppUpdateManager
    private lateinit var appUpdateInfo: AppUpdateInfo

    private val multiplePermissionsContract = ActivityResultContracts.RequestMultiplePermissions()
    private val multiplePermissionsLauncher =
        registerForActivityResult(multiplePermissionsContract) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setupListener()

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        appUpdateManager = AppUpdateManagerFactory.create(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showLogin(viewModel.loginVisible)
        setupViews()
        setupLoginEventsFlow()
        setupInactivityLogoutFlow()
    }

    override fun onResume() {
        super.onResume()
        if (sharedPreferencesDataSource.getAuthToken() == null && !binding.loginFragment.isVisible
            && !interactionDetector.shouldExtend
        ) {
            showLogoutDialog()
        }
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                updateApp()
            }
        }
    }

    private fun setupViews() {
        setupWebViewLogin()
        setupAppView()
    }

    override fun onUserInteraction() {
        window.decorView.performClick()
        return super.onUserInteraction()
    }

    private fun setupInactivityLogoutFlow() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                interactionDetector.userInteractionFlow.collect { event ->
                    if (event is UserInteractionEvent.Logout) {
                        showLogoutDialog()
                    }
                }
            }
        }
    }

    private fun setupLoginEventsFlow() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                webViewModel.loginEventsFlow.collect { loginEvent ->
                    when (loginEvent) {
                        LoginEvents.Logout -> {
                            showLogoutDialog()
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun showLogoutDialog() {
        BasicDialog(
            getString(R.string.logout_dialog_title),
            getString(R.string.logout_dialog_description)
        ) { logout() }.show(
            supportFragmentManager,
            BasicDialog::class.java.simpleName
        )
    }

    private fun setupWebViewLogin() {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(
                R.id.login_fragment,
                LoginFragment.newInstance {
                    showLogin(false)
                }
            )
        }
    }

    private fun setupAppView() {
        val navView: BottomNavigationView = binding.navView
        navView.isVisible = showBottomNav

        navController = findNavController(R.id.nav_host_fragment_activity_main)

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

        navView.setOnItemSelectedListener(object : OnItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                if (webViewModel.wasMerchantChanged) {
                    bottomNavItemIdSelected = item.itemId
                    webViewModel.wasMerchantChanged = false
                    showMerchantChangedDialog()
                    return false
                } else {
                    navController.navigate(item.itemId)
                    return true
                }
            }
        })

        supportActionBar?.hide()

        askPermissions()
    }

    private fun showMerchantChangedDialog() {
        BasicDialog(
            getString(R.string.merchant_changed_dialog_title),
            getString(R.string.merchant_changed_dialog_description)
        ) {
            navController.navigate(R.id.action_to_post_login)
        }.show(supportFragmentManager, BasicDialog::class.java.simpleName)
    }

    fun navigateToBottomNavItemSelected() {
        navController.navigate(bottomNavItemIdSelected)
    }

    fun checkForAppUpdate(enableUpdateButton: () -> Unit) {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            this.appUpdateInfo = appUpdateInfo
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(
                    AppUpdateType.IMMEDIATE
                )
            ) {
                enableUpdateButton()
            }
        }
    }

    fun logout() {
        interactionDetector.stopInactivityChecks()
        sharedPreferencesDataSource.setAuthToken(null)
        showLogin(true)
        setupWebViewLogin()
        navController.navigate(R.id.post_login_fragment)
    }

    private fun showLogin(show: Boolean) {
        if (viewModel.loginVisible != show) viewModel.loginVisible = show
        binding.container.isVisible = !show
        binding.loginFragment.isVisible = show
    }

    fun updateApp() {
        appUpdateManager.startUpdateFlow(
            appUpdateInfo, this, AppUpdateOptions.newBuilder(
                AppUpdateType.IMMEDIATE
            ).setAllowAssetPackDeletion(false).build()
        )
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