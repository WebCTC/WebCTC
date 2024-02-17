package org.webctc.common.types.railgroup

import kotlinx.serialization.Serializable
import kotlinx.uuid.UUID

@Serializable
data class RailGroupState(
    val uuid: UUID,
    val locked: Boolean,
    val reserved: Boolean,
    val trainOnRail: Boolean,
)
