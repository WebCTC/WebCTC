package org.webctc.common.types.railgroup

import kotlinx.serialization.Serializable
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID
import org.webctc.common.types.PosInt

@Serializable
data class RailGroup(
    val uuid: UUID = UUID.generateUUID(),
    var name: String = "Default Name",
    var railPosList: Set<PosInt> = setOf(),
    var rsPosList: Set<PosInt> = setOf(),
    var nextRailGroupList: Set<UUID> = setOf(),
    var displayPosList: Set<PosInt> = setOf(),
    var signalLevel: Int = 0
) {
    fun updateBy(other: RailGroup) {
        this.name = other.name
        this.railPosList = other.railPosList
        this.rsPosList = other.rsPosList
        this.nextRailGroupList = other.nextRailGroupList
        this.displayPosList = other.displayPosList
    }

    override fun equals(other: Any?): Boolean = other is RailGroup && this.uuid == other.uuid

    override fun hashCode() = uuid.hashCode()

    companion object
}
