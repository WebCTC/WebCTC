package org.webctc.common.types.railgroup

import kotlinx.serialization.Serializable

@Serializable
data class RailGroupState(
    val locked: Boolean,
    val reserved: Boolean,
    val trainOnRail: Boolean,
)
