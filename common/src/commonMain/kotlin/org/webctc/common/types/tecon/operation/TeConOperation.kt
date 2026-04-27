package org.webctc.common.types.tecon.operation

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import org.webctc.common.types.PosInt
import org.webctc.common.types.railgroup.RailGroupChain

@Polymorphic
interface ITeConOperation

@Serializable
data class ReserveOperation(
    val chain: RailGroupChain = RailGroupChain(),
    val key: String = "",
    val reservedRedStonePosSet: Set<PosInt> = emptySet(),
    val lockedRedStonePosSet: Set<PosInt> = emptySet(),
) : ITeConOperation

@Serializable
data class RedStoneOperation(
    val redStonePosSet: Set<PosInt> = emptySet(),
) : ITeConOperation

@Serializable
data class JavaScriptOperation(
    val script: String = "true",
) : ITeConOperation
