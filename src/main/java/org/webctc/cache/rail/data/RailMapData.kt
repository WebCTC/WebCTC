package org.webctc.cache.rail.data

import jp.ngt.rtm.rail.util.RailPosition

data class RailMapData(
    override val startRP: RailPosition,
    override val endRP: RailPosition,
    override val length: Double,
    override val neighborPos: NeighborPos
) : IRailMapData
