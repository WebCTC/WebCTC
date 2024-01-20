package org.webctc.common.types.rail

import kotlinx.serialization.Serializable

@Serializable
data class LargeRailData(val pos: IntArray, val isTrainOnRail: Boolean, val railMaps: List<IRailMapData>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as LargeRailData

        if (!pos.contentEquals(other.pos)) return false
        if (isTrainOnRail != other.isTrainOnRail) return false
        if (railMaps != other.railMaps) return false

        return true
    }

    override fun hashCode(): Int {
        var result = pos.contentHashCode()
        result = 31 * result + isTrainOnRail.hashCode()
        result = 31 * result + railMaps.hashCode()
        return result
    }
}
