package org.webctc.common.types.signal

import kotlinx.serialization.Serializable
import org.webctc.common.types.Pos

@Serializable
data class SignalData(
    val pos: Pos,
    val rotation: Float,
    val signalLevel: Int,
    val blockDirection: Int,
    val modelName: String
)
