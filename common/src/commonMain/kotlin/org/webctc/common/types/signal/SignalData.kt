package org.webctc.common.types.signal

import kotlinx.serialization.Serializable
import org.webctc.common.types.PosInt

@Serializable
data class SignalData(
    val pos: PosInt,
    val rotation: Float,
    val signalLevel: Int,
    val blockDirection: Int,
    val modelName: String
)
