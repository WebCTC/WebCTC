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
    var rsPosList: Set<PosIntWithKey> = setOf(),
    var nextRailGroupList: Set<UUID> = setOf(),
    var displayPosList: Set<PosInt> = setOf(),
    var switchSetting: SwitchSetting = SwitchSetting(),
    var signalLevel: Int = 0
) {
    fun updateBy(other: RailGroup) {
        this.name = other.name
        this.railPosList = other.railPosList
        this.rsPosList = other.rsPosList
        this.nextRailGroupList = other.nextRailGroupList
        this.displayPosList = other.displayPosList
        this.switchSetting = other.switchSetting
    }

    override fun equals(other: Any?): Boolean = other is RailGroup && this.uuid == other.uuid

    override fun hashCode() = uuid.hashCode()

    companion object
}

@Serializable
data class PosIntWithKey(val x: Int, val y: Int, val z: Int, val key: String = "") {
    constructor(pos: PosInt) : this(pos.x, pos.y, pos.z)

    fun toPosInt(): PosInt = PosInt(x, y, z)

    override fun toString(): String {
        return "$x,$y,$z"
    }

    companion object {
        val ZERO = PosIntWithKey(0, 0, 0)
    }
}