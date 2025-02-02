package org.webctc.common.types.tecon.shape

import kotlinx.serialization.Serializable
import org.webctc.common.types.PosInt2D
import kotlin.uuid.Uuid

@Serializable
data class RailLine(
    override val pos: PosInt2D = PosInt2D.ZERO,
    val start: PosInt2D = PosInt2D.ZERO,
    val end: PosInt2D,
    override val railGroupList: Set<Uuid> = setOf(),
    override val zIndex: Int = 0
) : RailShape