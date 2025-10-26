package org.webctc.common.types.tecon.shape

import kotlinx.serialization.Serializable
import org.webctc.common.types.PosInt2D
import kotlin.uuid.Uuid

@Serializable
data class TrainNumber(
    override val pos: PosInt2D = PosInt2D.ZERO,
    val start: PosInt2D = PosInt2D.ZERO,
    val end: PosInt2D,
    val railGroupList: Set<Uuid> = setOf(),
    val size: Int = 24,
    val anchor: String = "middle",
    override val zIndex: Int = 0
) : IShape
