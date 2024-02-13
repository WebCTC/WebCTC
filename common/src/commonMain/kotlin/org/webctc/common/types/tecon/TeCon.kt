package org.webctc.common.types.tecon

import kotlinx.serialization.Serializable
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID
import org.webctc.common.types.tecon.shape.IShape

@Serializable
data class TeCon(
    val uuid: UUID = UUID.generateUUID(),
    var name: String = "Default Name",
    var parts: List<IShape> = listOf(),
) {
    fun updateBy(teCon: TeCon) {
        name = teCon.name
        parts = teCon.parts
    }
}
