package org.webctc.common.types.rail

import kotlinx.serialization.Serializable

@Serializable
data class WebCTCRailPosition(
    val blockX: Int,
    val blockY: Int,
    val blockZ: Int,

    val switchType: Byte,
    val direction: Byte,
    val height: Byte,

    val posX: Double,
    val posY: Double,
    val posZ: Double,
)
