package org.webctc.common.types.railgroup

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class RailGroupState(
    val uuid: Uuid,
    val locked: Boolean,
    val reserved: Boolean,
    val trainOnRail: Boolean,
    val trainName: String?,
)
