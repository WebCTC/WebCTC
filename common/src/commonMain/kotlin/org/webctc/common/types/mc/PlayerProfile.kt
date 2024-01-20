package org.webctc.common.types.mc

import kotlinx.serialization.Serializable

@Serializable
data class PlayerProfile(
    val id: String,
    val name: String
)
