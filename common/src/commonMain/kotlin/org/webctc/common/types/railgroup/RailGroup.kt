package org.webctc.common.types.railgroup

import kotlinx.serialization.Serializable
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID
import org.webctc.common.types.PosInt

@Serializable
data class RailGroup(
    val uuid: UUID = UUID.generateUUID(),
    var name: String = "Default Name",
    val railPosList: MutableList<PosInt> = mutableListOf(),
    val rsPosList: MutableList<PosInt> = mutableListOf(),
    val nextRailGroupList: MutableList<UUID> = mutableListOf(),
    val displayPosList: MutableList<PosInt> = mutableListOf(),
    var signalLevel: Int = 0
) {
    fun updateBy(other: RailGroup) {
        this.name = other.name
        this.railPosList.clear()
        this.railPosList.addAll(other.railPosList)
        this.rsPosList.clear()
        this.rsPosList.addAll(other.rsPosList)
        this.nextRailGroupList.clear()
        this.nextRailGroupList.addAll(other.nextRailGroupList)
        this.displayPosList.clear()
        this.displayPosList.addAll(other.displayPosList)
    }

    override fun equals(other: Any?): Boolean = other is RailGroup && this.uuid == other.uuid

    override fun hashCode() = uuid.hashCode()

    companion object
}
