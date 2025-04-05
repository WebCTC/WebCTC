package org.webctc.common.types.tecon.shape

import kotlinx.serialization.Serializable
import org.webctc.common.types.PosInt2D

@Serializable
data class FreeText(
    override val pos: PosInt2D = PosInt2D.ZERO,
    val text: String = "FreeText",
    val size: Int = 12,
    val color: String = "white",
    val anchor: String = "start",
    override val zIndex: Int = 0
) : IShape