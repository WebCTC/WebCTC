package org.webctc.cache.rail.data

data class LargeRailData(val pos: IntArray, val isTrainOnRail: Boolean, val railMaps: List<IRailMapData>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

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
