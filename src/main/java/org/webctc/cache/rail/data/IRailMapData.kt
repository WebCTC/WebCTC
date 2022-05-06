package org.webctc.cache.rail.data

import jp.ngt.rtm.rail.util.RailPosition

interface IRailMapData {
    val startRP: RailPosition
    val endRP: RailPosition
    val length: Double
    val neighborPos: NeighborPos
}
