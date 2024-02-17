package org.webctc.common.types.waypoint.range

import kotlinx.serialization.Polymorphic
import org.webctc.common.types.PosDouble

@Polymorphic
interface IRange {
    fun contains(point: PosDouble): Boolean
}