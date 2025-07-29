// package com.xplore.paymobile.data.web
//
// import androidx.lifecycle.ViewModel
// import androidx.lifecycle.viewModelScope
// import dagger.hilt.android.lifecycle.HiltViewModel
// import kotlinx.coroutines.flow.MutableSharedFlow
// import kotlinx.coroutines.flow.SharedFlow
// import kotlinx.coroutines.flow.collectLatest
// import kotlinx.coroutines.launch
// import javax.inject.Inject
//
// @HiltViewModel
// open class WebEventsSharedViewModel @Inject constructor(
//    val jsBridge: JSBridge
// ) : ViewModel() {
//
//    var allowLogout = true
//    var wasMerchantChanged = false
//
//    private val _loginEventsFlow = MutableSharedFlow<LoginEvents>()
//    val loginEventsFlow: SharedFlow<LoginEvents> = _loginEventsFlow
//
//    private val _merchantChangesEventsFlow = MutableSharedFlow<MerchantChangesEvents>()
//    val merchantChangesEventsFlow: SharedFlow<MerchantChangesEvents> = _merchantChangesEventsFlow
//
//    init {
//        viewModelScope.launch {
//            jsBridge.jsBridgeFlows.loggedOutFlow.collectLatest { loggedOut ->
//                if (loggedOut == null) return@collectLatest
//
//                if (allowLogout) {
//                    allowLogout = false
//                    loggedOut.also {
//                        _loginEventsFlow.emit(LoginEvents.Logout)
//                    }
//                }
//            }
//        }
//
//        viewModelScope.launch {
//            jsBridge.jsBridgeFlows.userRolesFlow.collectLatest { userRoles ->
//                userRoles?.also {
//                    _loginEventsFlow.emit(LoginEvents.LoginSuccessful(userRoles))
//                }
//            }
//        }
//
//        viewModelScope.launch {
//            jsBridge.jsBridgeFlows.merchantFlow.collectLatest { merchant ->
//                merchant?.also {
//                    _merchantChangesEventsFlow.emit(MerchantChangesEvents.MerchantChanged)
//                }
//            }
//        }
//    }
// }
//
// sealed class LoginEvents {
//    data class LoginSuccessful(var userRoles: UserRoles) : LoginEvents()
//    object Logout : LoginEvents()
// }
//
// sealed class MerchantChangesEvents {
//    object MerchantChanged : MerchantChangesEvents()
// }
