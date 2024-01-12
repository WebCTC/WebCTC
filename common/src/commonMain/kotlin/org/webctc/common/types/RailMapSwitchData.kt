package org.webctc.common.types

import kotlinx.serialization.Serializable

@Serializable
data class RailMapSwitchData(
    override val startRP: WebCTCRailPosition,
    override val endRP: WebCTCRailPosition,
    override val length: Double,
    override val neighborPos: NeighborPos,
    val isNotActive: Boolean
) : IRailMapData
