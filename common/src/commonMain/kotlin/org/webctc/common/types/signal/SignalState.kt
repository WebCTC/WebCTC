package org.webctc.common.types.signal

import kotlinx.serialization.Serializable
import org.webctc.common.types.PosInt

@Serializable
data class SignalState(
    val pos: PosInt,
    val level: Int,
)
