package org.webctc.common.types.tecon.shape

import kotlinx.serialization.Serializable
import org.webctc.common.types.PosInt2D

@Serializable
data class Route(
    val pos: PosInt2D,
    val name: String = "",
    override val zIndex: Int = 0
) : IShape