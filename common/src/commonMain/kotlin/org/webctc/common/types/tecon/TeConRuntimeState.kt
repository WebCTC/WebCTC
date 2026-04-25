package org.webctc.common.types.tecon

import kotlinx.serialization.Serializable

@Serializable
data class TeConRuntimeState(
    val leverStates: List<TeConLeverRuntimeState> = emptyList(),
)

@Serializable
data class TeConLeverRuntimeState(
    val leverId: String,
    val activeSide: TeConLeverSide? = null,
    val routeId: String? = null,
    val actionName: String = "",
    val cancelable: Boolean = false,
)
