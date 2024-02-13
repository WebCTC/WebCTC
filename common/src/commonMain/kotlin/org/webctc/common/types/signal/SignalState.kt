package org.webctc.common.types.signal

import kotlinx.serialization.Serializable

@Serializable
data class SignalState(
    val level: Int,
)
