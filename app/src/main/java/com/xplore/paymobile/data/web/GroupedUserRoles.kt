package com.xplore.paymobile.data.web

sealed class GroupedUserRoles(val roles: List<String>? = null) {
    object VirtualTerminalRoles : GroupedUserRoles(
        listOf(
            "VirtualTerminalUser",
            "VTAccountAnalysts",
            "VTAccountOwners",
            "VTAccountAdministrators",
            "VTClearentSupport",
            "VTClearentAdministrators"
        )
    )
    object MerchantHomeRoles : GroupedUserRoles(listOf("MerchantHomeUser", "CustomerSupport", "SalesRep"))
    object NoAccess : GroupedUserRoles(listOf(""))

    companion object {
        fun fromString(value: List<String>) = GroupedUserRoles::class.sealedSubclasses.find {
            it.objectInstance?.roles!!.intersect(value).isNotEmpty()
        }?.objectInstance ?: NoAccess
    }
}