package org.webctc.common.types

import kotlinx.serialization.Serializable

@Serializable
data class NeighborPos(val startRP: Pos, val endRP: Pos)
