package org.webctc.common.types.tecon.shape

import kotlinx.serialization.Serializable
import org.webctc.common.types.PosInt2D
import kotlin.uuid.Uuid

@Serializable
data class RailPolyLine(
    override val pos: PosInt2D = PosInt2D.ZERO,
    val points: List<PosInt2D>,
    override val railGroupList: Set<Uuid> = setOf(),
    override val zIndex: Int = 0
) : RailShape