package org.webctc.common.types.rail

import kotlinx.serialization.Serializable
import org.webctc.common.types.PosInt

@Serializable
data class LargeRailData(
    val pos: PosInt,
    val isTrainOnRail: Boolean = false,
    val railMaps: List<IRailMapData>,
    val converting: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as LargeRailData

        if (pos != other.pos) return false
        if (isTrainOnRail != other.isTrainOnRail) return false
        if (railMaps != other.railMaps) return false
        if (converting != other.converting) return false

        return true
    }

    override fun hashCode() = pos.hashCode()
}
