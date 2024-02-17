package org.webctc.common.types.rail

import kotlinx.serialization.Polymorphic

@Polymorphic
interface IRailMapData {
    val startRP: WebCTCRailPosition
    val endRP: WebCTCRailPosition
    val length: Double
    val neighborPos: NeighborPos
}
