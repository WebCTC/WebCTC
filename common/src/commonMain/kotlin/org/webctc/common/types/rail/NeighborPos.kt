package org.webctc.common.types.rail

import kotlinx.serialization.Serializable
import org.webctc.common.types.Pos

@Serializable
data class NeighborPos(val startRP: Pos, val endRP: Pos)
