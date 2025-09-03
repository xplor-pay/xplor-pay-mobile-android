package com.xplore.paymobile.data.web

sealed class GroupedUserRoles(val roles: Set<String>) {
    object VirtualTerminalRoles : GroupedUserRoles(
        setOf(
            "VirtualTerminalUser",
            "VTAccountAnalysts", // void/refund
            "VTAccountOwners", // void/refund
            "VTAccountAdministrators", // void/refund
            "VTClearentSupport", // do not think we need for validation
            "VTClearentAdministrators", // void/refund
        ),
    )

    object NoAccess : GroupedUserRoles(setOf(""))

    companion object {
        fun fromString(value: String): GroupedUserRoles {
            // todo not the best implementation but it works for now
            val roles = value.replace("[", "").replace("]", "").split(",")
            println(roles)
            for (role in roles) {
                if (VirtualTerminalRoles.roles.contains(role)) {
                    return VirtualTerminalRoles
                }
            }
            println("**************************no access")
            return NoAccess
        }
    }
}
