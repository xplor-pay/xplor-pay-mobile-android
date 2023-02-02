package com.xplore.paymobile.interactiondetection

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import timber.log.Timber
import javax.inject.Inject

class UserInteractionDetector @Inject constructor() {

    companion object {
        private const val INACTIVITY_TIME = 1000 * 60 * 15L //15 minutes
        private const val CHECK_INTERVAL = 1000 * 10L //10 seconds
    }

    private val bgScope = CoroutineScope(Dispatchers.IO)
    private var timerJob: Job? = null

    private var lastInteraction = System.currentTimeMillis()

    private val _userInteractionFlow = MutableSharedFlow<UserInteractionEvent?>()
    val userInteractionFlow: SharedFlow<UserInteractionEvent?> = _userInteractionFlow

    var shouldExtend = false

    fun onUserInteraction() {
        lastInteraction = System.currentTimeMillis()
    }

    fun stopInactivityChecks() {
        bgScope.launch {
            if (timerJob?.isActive == true) {
                Timber.d("Cancel inactivity timer")
                timerJob?.cancelAndJoin()
            }
        }
    }

    fun launchInactivityChecks() {
        timerJob = bgScope.launch {
            while (true) {
                delay(CHECK_INTERVAL)
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastInteraction > INACTIVITY_TIME) {
                    shouldExtend = false
                    logout()
                } else {
                    shouldExtend = true
                }
            }
        }
    }

    private fun logout() {
        bgScope.launch {
            Timber.d("Force logout due to inactivity")
            _userInteractionFlow.emit(UserInteractionEvent.Logout)
        }
    }

    fun onSessionExpirationEvent() {
        bgScope.launch {
            if (shouldExtend) {
                Timber.d("Extend webview session")
                _userInteractionFlow.emit(UserInteractionEvent.ExtendSession)
                shouldExtend = false
            }
        }
    }
}

sealed class UserInteractionEvent {
    object ExtendSession : UserInteractionEvent()
    object Logout : UserInteractionEvent()
}