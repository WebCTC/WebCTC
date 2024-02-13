package org.webctc.common.types.tecon.shape

import kotlinx.serialization.Serializable
import org.webctc.common.types.PosInt
import org.webctc.common.types.PosInt2D

@Serializable
data class Signal(
    val pos: PosInt2D,
    val signalPos: PosInt = PosInt.ZERO,
    override val zIndex: Int = 0
) : IShape