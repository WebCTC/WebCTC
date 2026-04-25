package org.webctc.common.types.tecon.shape

import kotlinx.serialization.Serializable
import org.webctc.common.types.PosInt2D

@Serializable
data class TeConLever(
    override val pos: PosInt2D = PosInt2D.ZERO,
    val id: String = "",
    val name: String = "",
    val rotation: Int = 0,
    val left: TeConLeverSideConfig = DisabledLeverSide(),
    val right: TeConLeverSideConfig = DisabledLeverSide(),
    override val zIndex: Int = 0
) : IShape
