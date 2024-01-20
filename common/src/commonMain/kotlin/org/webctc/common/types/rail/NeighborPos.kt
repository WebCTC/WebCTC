package org.webctc.common.types.rail

import kotlinx.serialization.Serializable
import org.webctc.common.types.PosInt

@Serializable
data class NeighborPos(val startRP: PosInt, val endRP: PosInt)
