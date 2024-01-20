package org.webctc.common.types.mc

import kotlinx.serialization.Serializable


@Serializable
data class PlayerPrinciple(
    val name: String,
    val uuid: String,
)