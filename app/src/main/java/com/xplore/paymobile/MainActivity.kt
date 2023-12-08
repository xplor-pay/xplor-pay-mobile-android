package com.xplore.paymobile

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.clearent.idtech.android.wrapper.ClearentDataSource
import com.clearent.idtech.android.wrapper.ClearentWrapper
import com.clearent.idtech.android.wrapper.listener.MerchantAndTerminalRequestedListener
import com.clearent.idtech.android.wrapper.model.NetworkStatus
import com.clearent.idtech.android.wrapper.ui.util.checkPermissionsToRequest
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.xplore.paymobile.data.datasource.SharedPreferencesDataSource
import com.xplore.paymobile.databinding.ActivityMainBinding
import com.xplore.paymobile.interactiondetection.UserInteractionDetector
import com.xplore.paymobile.ui.FirstPairListener
import com.xplore.paymobile.ui.dialog.BasicDialog
import com.xplore.paymobile.ui.login.BrowserState
import com.xplore.paymobile.ui.login.OktaLoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), FirstPairListener, MerchantAndTerminalRequestedListener {

    private val viewModel by viewModels<MainViewModel>()
    private val oktaLoginViewModel by viewModels<OktaLoginViewModel>()

    @Inject
    lateinit var sharedPreferencesDataSource: SharedPreferencesDataSource

    @Inject
    lateinit var interactionDetector: UserInteractionDetector

    companion object {
        private const val HINTS_DISPLAY_DELAY = 3000L
        private const val FORCE_LOGIN_DIALOG_TAG = "InternetDialog"
    }

    private val clearentWrapper = ClearentWrapper.getInstance()

    private var hintsShowed = false
    private var showBottomNav = true
    private var bottomNavItemIdSelected = R.id.navigation_payment
    private var isResumed: Boolean = false

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    private lateinit var appUpdateManager: AppUpdateManager
    private lateinit var appUpdateInfo: AppUpdateInfo

    private val multiplePermissionsContract = ActivityResultContracts.RequestMultiplePermissions()
    private val multiplePermissionsLauncher =
        registerForActivityResult(multiplePermissionsContract) {}
// todo let's not show the merchant select screen before the user is logged in
    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation =
            if (resources.getBoolean(R.bool.isTablet)) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        installSplashScreen()

        super.onCreate(savedInstanceState)

        setupListener()

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        appUpdateManager = AppUpdateManagerFactory.create(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showLogin(viewModel.loginVisible)
        setupAppView()
//        setupInactivityLogoutFlow()
//        setupNetworkFlow()
//        clearentWrapper.addMerchantAndTerminalRequestedListener(this)
    }

    //todo sometimes the app logs out but immediately logs back in.
    // need to find a way to revoke or delete the credential token.
    // sometimes the app opens the web browser multiple times when logging in.
    // the user can still press the "Continue to Xplor Pay app" to be redirected back to the app...not optimal
    private fun startOktaFlow() {
        oktaLoginViewModel.state.observe(this) { state ->
//            Logger.logMessage("Attempting to login.")
            when (state) {
                is BrowserState.LoggedIn -> {
                    if (!oktaLoginViewModel.isLoggingIn && !sharedPreferencesDataSource.isLoggedIn()) {
                        println("++++++++++++++++++++++++++Attempting to login.")
//                        oktaLoginViewModel.login(this)
                        viewModel.loginVisible = true

                        //todo setup the inactivity flow 
//                        setupInactivityLogoutFlow()
//                        setupAppView()
                        setupNetworkFlow()
                        clearentWrapper.addMerchantAndTerminalRequestedListener(this)
                        navController.navigate(R.id.action_to_post_login)
                    }
                }
                is BrowserState.LoggedOut -> {
                    println("===========================Attempting to logout.")
                    viewModel.loginVisible = false
//                    if (state.errorMessage == "Flow cancelled.") {
//                        this.finishAffinity()
//                    } else {
//                    navController.navigate(R.id.action_to_post_login)
                    oktaLoginViewModel.login(this)
//                    }
                }
                is BrowserState.Loading -> {
                    println("----------------------------loading")
//                    oktaLoginViewModel.login(this)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        isResumed = true
        //todo remove this.  if the user does not accept permissions, check permissions before initiating transaction
//        if (clearentWrapper.hasAppPermissions() && !sharedPreferencesDataSource.isLoggedIn()) {
        if (clearentWrapper.hasAppPermissions() && sharedPreferencesDataSource.isSdkSetUp() && !sharedPreferencesDataSource.isLoggedIn()) {
            startOktaFlow()
            if (sharedPreferencesDataSource.isLoggedIn()) {
                interactionDetector.launchInactivityChecks()
            }
        }
        if (viewModel.shouldShowForceLoginDialog && !binding.loginFragment.isVisible) {
            showForceLoginDialog()
        }
        if (!binding.loginFragment.isVisible && !interactionDetector.shouldExtend) {
            showLogoutDialog()
        }
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                updateApp()
            }
        }
    }

    override fun onUserInteraction() {
        window.decorView.performClick()
        return super.onUserInteraction()
    }

    //todo will we want to implement in the future?
//    private fun setupInactivityLogoutFlow() {
//        lifecycleScope.launch {
//            repeatOnLifecycle(Lifecycle.State.STARTED) {
//                viewModel. .collect { loginEvent ->
//                    when (loginEvent) {
//                        LoginEvents.Logout -> {
//                            showLogoutDialog()
//                        }
//                        else -> {}
//                    }
//                }
//            }
//        }
//    }

    private fun showLogoutDialog() {
        interactionDetector.stopInactivityChecks()
        BasicDialog(title = getString(R.string.logout_dialog_title),
            message = getString(R.string.logout_dialog_description),
            positiveButton = BasicDialog.DialogButton(getString(R.string.ok)) {},
            onDismiss = {
                logout()
            }).show(
            supportFragmentManager, BasicDialog::class.java.simpleName
        )
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

        navView.setOnItemSelectedListener { item ->
            navController.navigate(item.itemId)
            true
        }

        supportActionBar?.hide()

        askPermissions()
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
        oktaLoginViewModel.logout(this)
        sharedPreferencesDataSource.setAuthToken(null)
    }

    private fun showLogin(show: Boolean) {
        if (viewModel.loginVisible != show) viewModel.loginVisible = show
        binding.container.isVisible = !show
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


    private fun setupNetworkFlow() {
        lifecycleScope.launch {
            ClearentDataSource.networkStatusFlow.collect { networkStatus ->
                val isInOfflineMode = clearentWrapper.storeAndForwardEnabled
                if (networkStatus is NetworkStatus.CapabilitiesChanged) return@collect
                if (!isResumed && isInOfflineMode) {
                    when (networkStatus) {
                        is NetworkStatus.Available -> viewModel.shouldShowForceLoginDialog = true
                        is NetworkStatus.Lost -> viewModel.shouldShowForceLoginDialog = false
                        else -> {}
                    }
                    return@collect
                }
                if (networkStatus is NetworkStatus.Available && isInOfflineMode && !binding.loginFragment.isVisible) {
                    showForceLoginDialog()
                }
            }
        }
    }

    override fun onMerchantAndTerminalRequested() {
        val merchant = sharedPreferencesDataSource.getMerchant()?.merchantName
        val terminal = sharedPreferencesDataSource.getTerminal()?.terminalName
        clearentWrapper.provideMerchantAndTerminalName(merchant, terminal)
    }

    private fun showForceLoginDialog() {
        val isShown =
            supportFragmentManager.findFragmentByTag(FORCE_LOGIN_DIALOG_TAG)?.isAdded ?: false
        if (isShown) return
        BasicDialog(getString(R.string.logout_dialog_title),
            getString(R.string.internet_restored),
            BasicDialog.DialogButton(getString(R.string.ok)) {
                logout()
                clearentWrapper.storeAndForwardEnabled = false
            }).show(
            supportFragmentManager, FORCE_LOGIN_DIALOG_TAG
        )
    }

    override fun onPause() {
        super.onPause()
        isResumed = false
    }

    override fun onDestroy() {
        super.onDestroy()
        clearentWrapper.removeListener()
        clearentWrapper.removeMerchantAndTerminalRequestedListener(this)
    }

    override fun showFirstPair(onClick: () -> Unit, onDismiss: () -> Unit) {
        if (hintsShowed) return

        hintsShowed = true
        lifecycle.addObserver(ListenerCallbackObserver(lifecycle) {
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
        })
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
        private val lifecycle: Lifecycle, private val callback: () -> Unit
    ) : DefaultLifecycleObserver, LifecycleObserver {

        override fun onStart(owner: LifecycleOwner) {
            callback()
            lifecycle.removeObserver(this)
        }
    }
}