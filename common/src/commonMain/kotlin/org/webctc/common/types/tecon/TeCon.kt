package org.webctc.common.types.tecon

import kotlinx.serialization.Serializable
import org.webctc.common.types.tecon.shape.IShape
import kotlin.uuid.Uuid

@Serializable
data class TeCon(
    val uuid: Uuid = Uuid.random(),
    var name: String = "Default Name",
    var parts: List<IShape> = listOf(),
) {
    fun updateBy(teCon: TeCon) {
        name = teCon.name
        parts = teCon.parts
    }
}
