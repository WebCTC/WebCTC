package org.webctc.common.types.tecon

import kotlinx.serialization.Serializable

@Serializable
enum class TeConLeverSide {
    L,
    R,
}

@Serializable
enum class TeConOperateActionType {
    REQUEST,
    RETURN,
}

@Serializable
data class TeConOperateRequest(
    val leverId: String,
    val side: TeConLeverSide,
    val routeId: String? = null,
    val actionType: TeConOperateActionType,
)

@Serializable
data class TeConOperateResponse(
    val ok: Boolean,
    val message: String = "",
    val runtimeState: TeConRuntimeState = TeConRuntimeState(),
)
