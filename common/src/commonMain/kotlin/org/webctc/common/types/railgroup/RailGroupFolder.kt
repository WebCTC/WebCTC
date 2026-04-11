package org.webctc.common.types.railgroup

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class RailGroupFolder(
    val uuid: Uuid = Uuid.random(),
    var name: String = "New Folder",
    var parentUuid: Uuid? = null
) {
    fun updateBy(other: RailGroupFolder) {
        this.name = other.name
        this.parentUuid = other.parentUuid
    }

    override fun equals(other: Any?): Boolean = other is RailGroupFolder && this.uuid == other.uuid

    override fun hashCode() = uuid.hashCode()

    companion object
}
