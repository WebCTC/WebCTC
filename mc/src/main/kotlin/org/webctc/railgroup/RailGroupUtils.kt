package org.webctc.railgroup

import jp.ngt.rtm.electric.TileEntitySignal
import kotlinx.uuid.UUID
import net.minecraft.init.Blocks
import net.minecraft.nbt.NBTBase
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.nbt.NBTTagString
import net.minecraft.server.MinecraftServer
import org.webctc.cache.rail.RailCacheData
import org.webctc.cache.readFromNBT
import org.webctc.cache.writeToNBT
import org.webctc.common.types.PosInt
import org.webctc.common.types.railgroup.RailGroup


fun RailGroup.isTrainOnRail(): Boolean {
    return railPosList
        .mapNotNull { RailCacheData.railMapCache[it] }
        .any { it.isTrainOnRail }
}

fun UUID.writeToNBT(): NBTTagString {
    return NBTTagString(this.toString())
}

fun RailGroup.Companion.create(): RailGroup {
    val railGroup = RailGroup()
    RailGroupData.railGroupList.add(railGroup)
    return railGroup
}

fun RailGroup.delete(): Boolean {
    return RailGroupData.railGroupList.remove(this)
}

fun RailGroup.Companion.delete(uuid: UUID): Boolean {
    return RailGroupData.railGroupList.removeAll { it.uuid == uuid }
}

fun RailGroup.writeToNBT(): NBTTagCompound {
    val tag = NBTTagCompound()
    tag.setString("name", this.name)
    tag.setString("uuid", this.uuid.toString())

    railPosList.map { it.writeToNBT() }
        .toNBTTagList()
        .let { tag.setTag("railPosTagList", it) }

    rsPosList.map { it.writeToNBT() }
        .toNBTTagList()
        .let { tag.setTag("rsPosTagList", it) }

    nextRailGroupList.map { it.writeToNBT() }
        .toNBTTagList()
        .let { tag.setTag("nextRailGroupTagList", it) }

    displayPosList.map { it.writeToNBT() }
        .toNBTTagList()
        .let { tag.setTag("displayPosTagList", it) }

    return tag
}

fun RailGroup.Companion.readFromNBT(nbt: NBTTagCompound): RailGroup {
    val uuid = UUID(nbt.getString("uuid"))
    val name = nbt.getString("name")

    val railPosList = nbt.getTagList("railPosTagList", 10)
        .toList()
        .map { PosInt.readFromNBT(it) }
        .toMutableList()

    val rsPosList = nbt.getTagList("rsPosTagList", 10)
        .toList()
        .map { PosInt.readFromNBT(it) }
        .toMutableList()

    val nextRailGroupList = nbt.getTagList("nextRailGroupTagList", 8)
        .toStringList()
        .map { UUID(it) }
        .toMutableList()

    val displayPosList = nbt.getTagList("displayPosTagList", 10)
        .toList()
        .map { PosInt.readFromNBT(it) }
        .toMutableList()

    val railGroup = RailGroup(
        uuid,
        name,
        railPosList,
        rsPosList,
        nextRailGroupList,
        displayPosList
    )

    return railGroup
}

fun RailGroup.update() {
    val isTrainOnRail = this.isTrainOnRail()
    this.signalLevel = ((if (isTrainOnRail) 0
    else this.nextRailGroupList
        .mapNotNull { uuid -> RailGroupData.railGroupList.find { it.uuid == uuid } }
        .minOfOrNull { it.signalLevel } ?: 0) + 1).coerceAtMost(6)

    val world = MinecraftServer.getServer().entityWorld

    this.displayPosList
        .map { world.getTileEntity(it.x, it.y, it.z) }
        .filterIsInstance<TileEntitySignal>()
        .filter { it.signalLevel != this.signalLevel }
        .forEach { it.setElectricity(it.xCoord, it.yCoord, it.zCoord, this.signalLevel) }

    val block = if (isTrainOnRail) Blocks.redstone_block else Blocks.stained_glass
    this.rsPosList.forEach { world.setBlock(it.x, it.y, it.z, block, 14, 3) }
}

val TileEntitySignal.signalLevel: Int
    get() = this.javaClass.fields
        .find { it.name == "signalLevel" }
        ?.apply { this.isAccessible = true }
        ?.get(this) as? Int ?: 0

fun List<NBTBase>.toNBTTagList(): NBTTagList {
    return NBTTagList().apply {
        this@toNBTTagList.forEach { this.appendTag(it) }
    }
}

fun NBTTagList.toList(): List<NBTTagCompound> {
    return (0 until this.tagCount()).map { this.getCompoundTagAt(it) }
}

fun NBTTagList.toStringList(): List<String> {
    return (0 until this.tagCount()).map { this.getStringTagAt(it) }
}