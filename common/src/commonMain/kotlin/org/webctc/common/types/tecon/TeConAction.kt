package org.webctc.common.types.tecon

import kotlinx.serialization.Serializable
import org.webctc.common.types.tecon.operation.ITeConOperation

@Serializable
data class TeConAction(
    val routeId: String? = null,
    val name: String = "Action",
    val requireNoTrainToCancel: Boolean = false,
    val operations: Set<ITeConOperation> = emptySet(),
)
