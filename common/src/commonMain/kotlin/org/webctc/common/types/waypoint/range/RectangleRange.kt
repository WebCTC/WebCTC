package org.webctc.common.types.waypoint.range

import kotlinx.serialization.Serializable
import org.webctc.common.types.PosDouble
import kotlin.math.abs

@Serializable
data class RectangleRange(
    var start: PosDouble,
    var end: PosDouble
) : IRange {
    val height = abs(start.z - end.z)
    val width = abs(start.x - end.x)

    val minX = minOf(start.x, end.x)
    val maxX = maxOf(start.x, end.x)
    val minZ = minOf(start.z, end.z)
    val maxZ = maxOf(start.z, end.z)

    override fun contains(point: PosDouble): Boolean {
        return point.x in minX..maxX && point.z in minZ..maxZ
    }
}