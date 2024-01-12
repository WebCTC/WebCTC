package org.webctc.common.types

interface IRailMapData {
    val startRP: WebCTCRailPosition
    val endRP: WebCTCRailPosition
    val length: Double
    val neighborPos: NeighborPos
}
