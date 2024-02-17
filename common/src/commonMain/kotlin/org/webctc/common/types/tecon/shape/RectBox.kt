package org.webctc.common.types.tecon.shape

import kotlinx.serialization.Serializable
import org.webctc.common.types.PosInt2D

@Serializable
data class RectBox(
    val start: PosInt2D,
    val end: PosInt2D,
    override val zIndex: Int = 0
) : IShape
