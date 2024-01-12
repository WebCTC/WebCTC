package org.webctc.common.types

import kotlinx.serialization.Serializable

@Serializable
data class WayPoint(val identifyName: String, val displayName: String, val pos: Pos)
