package org.webctc.common.types.waypoint

import kotlinx.serialization.Serializable
import org.webctc.common.types.PosDouble
import org.webctc.common.types.waypoint.range.CircleRange
import org.webctc.common.types.waypoint.range.IRange

@Serializable
data class WayPoint(
    val identifyName: String,
    var displayName: String = "",
    var pos: PosDouble = PosDouble.ZERO,
    var range: IRange = CircleRange(pos, 100.0)
) {
    fun updateBy(other: WayPoint) {
        this.displayName = other.displayName
        this.pos = other.pos
        this.range = other.range
    }

    override fun equals(other: Any?): Boolean = other is WayPoint && this.identifyName == other.identifyName

    override fun hashCode() = identifyName.hashCode()

    companion object
}


