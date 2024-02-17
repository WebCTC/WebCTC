package org.webctc.common.types.rail

import kotlinx.serialization.Serializable

@Serializable
data class RailMapData(
    override val startRP: WebCTCRailPosition,
    override val endRP: WebCTCRailPosition,
    override val length: Double,
    override val neighborPos: NeighborPos
) : IRailMapData
