package com.xplore.paymobile.data.web

sealed class GroupedUserRoles(val roles: Set<String>) {
    object VirtualTerminalRoles : GroupedUserRoles(
        setOf(
            "VirtualTerminalUser",
            "VTAccountAnalysts", //void/refund
            "VTAccountOwners", //void/refund
            "VTAccountAdministrators", //void/refund
            "VTClearentSupport", //do not think we need for validation
            "VTClearentAdministrators" //void/refund
        )
    )
//todo remove MH roles unless review of app reveals a need for the MH roles
    object MerchantHomeRoles : GroupedUserRoles(
        setOf(
            "MerchantHomeUser",
            "CustomerSupport",
            "SalesRep"
        )
    )

    object NoAccess : GroupedUserRoles(setOf(""))

    companion object {
        fun fromString(value: List<String>): GroupedUserRoles {
            // Manual check due to reflection ordering unpredictability
            // The user may have multiple roles but role priority must be preserved
            if (VirtualTerminalRoles.roles.intersect(value.toSet()).isNotEmpty())
                return VirtualTerminalRoles

            if (MerchantHomeRoles.roles.intersect(value.toSet()).isNotEmpty())
                return MerchantHomeRoles

            return NoAccess
        }
    }
}