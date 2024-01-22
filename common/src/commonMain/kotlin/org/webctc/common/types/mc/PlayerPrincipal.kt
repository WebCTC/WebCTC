package org.webctc.common.types.mc

import kotlinx.serialization.Serializable


@Serializable
data class PlayerPrincipal(
    val name: String,
    val uuid: String,
)