package org.webctc.common.types.trains

import kotlinx.serialization.Serializable


@Serializable
data class FormationData(
    val id: Long,
    val entities: List<FormationEntityData>,
    val controlCar: TrainData?,
    val driver: String,
    val direction: Byte,
    val speed: Float
)

@Serializable
data class FormationEntityData(
    val train: Int,
    val entryId: Byte,
    val dir: Byte
)