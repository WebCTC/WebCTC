package org.webctc.common.types.waypoint.range

import kotlinx.serialization.Serializable
import org.webctc.common.types.PosDouble

@Serializable
data class CircleRange(
    var center: PosDouble,
    var radius: Double
) : IRange {
    override fun contains(point: PosDouble): Boolean {
        val dx = point.x - center.x
        val dz = point.z - center.z
        return dx * dx + dz * dz <= radius * radius
    }
}
