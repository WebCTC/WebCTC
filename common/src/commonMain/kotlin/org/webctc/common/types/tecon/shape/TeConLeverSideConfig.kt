package org.webctc.common.types.tecon.shape

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import org.webctc.common.types.tecon.TeConAction

@Polymorphic
interface TeConLeverSideConfig

@Serializable
class DisabledLeverSide : TeConLeverSideConfig {
    override fun equals(other: Any?) = other is DisabledLeverSide

    override fun hashCode(): Int = 31
}

@Serializable
data class DirectLeverSide(
    val action: TeConAction = TeConAction(),
) : TeConLeverSideConfig

@Serializable
data class SelectLeverSide(
    val actions: Set<TeConAction> = emptySet(),
) : TeConLeverSideConfig
