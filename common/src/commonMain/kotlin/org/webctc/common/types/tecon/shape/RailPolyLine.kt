package org.webctc.common.types.tecon.shape

import kotlinx.serialization.Serializable
import kotlinx.uuid.UUID
import org.webctc.common.types.PosInt2D

@Serializable
data class RailPolyLine(
    val points: List<PosInt2D>,
    override val railGroupList: Set<UUID> = setOf(),
    override val zIndex: Int = 0
) : RailShape