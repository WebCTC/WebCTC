package org.webctc.common.types.trains

import kotlinx.serialization.Serializable

@Serializable
data class TrainData(
    val formation: Long?,
    val id: Int,
    val speed: Float,
    val notch: Int,
    val modelName: String,
    val isControlCar: Boolean,
    val signal: Int,
    val driver: String,
    val passengers: List<String?>,
    val pos: List<Double>,
    val trainStateData: ByteArray,
    val name: String,
    val customButton: List<CustomButtonData>,
//    val dataMap: Map<String, Any?>
)

@Serializable
data class CustomButtonData(
    val value: Int,
    val text: String?
)