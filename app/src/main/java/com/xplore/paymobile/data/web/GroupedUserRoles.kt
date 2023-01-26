package com.xplore.paymobile.data.web

sealed class GroupedUserRoles(val roles: Set<String>) {
    object VirtualTerminalRoles : GroupedUserRoles(
        setOf(
            "VirtualTerminalUser",
            "VTAccountAnalysts",
            "VTAccountOwners",
            "VTAccountAdministrators",
            "VTClearentSupport",
            "VTClearentAdministrators"
        )
    )

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