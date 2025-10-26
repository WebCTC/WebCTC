package org.webctc.common.types.trains

import kotlinx.serialization.Serializable
import org.webctc.common.types.PosInt


@Serializable
data class FormationData(
    val id: Long,
    val entities: List<FormationEntityData>,
    val controlCar: TrainData?,
    val driver: String,
    val direction: Byte,
    val speed: Float,
    val currentRailMap: PosInt?
)

@Serializable
data class FormationEntityData(
    val train: Int,
    val entryId: Byte,
    val dir: Byte
)