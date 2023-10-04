package com.xplore.paymobile.data.web

import com.clearent.idtech.android.wrapper.ClearentCredentials
import com.clearent.idtech.android.wrapper.ClearentWrapper
import com.xplore.paymobile.data.datasource.NetworkResource
import com.xplore.paymobile.data.datasource.RemoteDataSource
import com.xplore.paymobile.data.datasource.SharedPreferencesDataSource
import com.xplore.paymobile.data.remote.model.TerminalsResponse
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

class VTRefreshManager @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val sharedPrefs: SharedPreferencesDataSource
) {

    companion object {
        const val VT_REFRESH_TIME = 1000*60*10L
    }

    private val timerScope = CoroutineScope(Dispatchers.IO)
    private var timerJob: Job? = null

    fun startTimer(refreshFirst: Boolean = true) {
        Timber.d("Start VT refresh timer")
        timerScope.launch {
            if (timerJob?.isActive == true) {
                Timber.d("Cancel previous timer")
                timerJob?.cancelAndJoin()
            }
            if (refreshFirst) refreshToken()
            launchTimer()
        }
    }

    //TODO could we check the auth token in the same fashion?
    private fun launchTimer() {
        Timber.d("Launch VT refresh timer")
        timerJob = timerScope.launch {
            while (true) {
                delay(VT_REFRESH_TIME)
                refreshToken()
            }
        }
    }

    private suspend fun refreshToken() {
        //TODO issue with refresh token using the defaulted terminal...jwt expired?  not 100% this is an issue...will we even need this class?
        Timber.d("Start token refresh")
        val merchant = sharedPrefs.getMerchant() ?: return
        val terminal = sharedPrefs.getTerminal() ?: return

        val terminalsResponse = remoteDataSource.fetchTerminals(merchant.merchantNumber)
        if (terminalsResponse is NetworkResource.Success<TerminalsResponse?>) {
            val newTerminal = terminalsResponse.data?.find { found ->
                found.terminalPKId == terminal.terminalPKId
            }
            newTerminal?.let {
                Timber.d("Token refresh successful")
                ClearentWrapper.getInstance().sdkCredentials.clearentCredentials  =
                    ClearentCredentials.MerchantHomeApiCredentials(
                        merchantId = merchant.merchantNumber,
                        vtToken = newTerminal.questJwt.token
                    )
            }
        }
    }
}