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
import org.webctc.common.types.rail.RailMapSwitchData
import org.webctc.common.types.railgroup.*

fun RailGroup.isTrainOnRail(): Boolean {
    return railPosList
        .mapNotNull { RailCacheData.railMapCache[it] }
        .any { it.isTrainOnRail }
}

fun RailGroup.isLocked(): Boolean {
    return RailGroupData.isLocked(this.uuid)
}

fun RailGroup.isReserved(): Boolean {
    return RailGroupData.isReserved(this.uuid)
}

fun RailGroup.getState(): RailGroupState {
    val isTrainOnRail = this.isTrainOnRail()
    val isReserved = this.isReserved()
    val isLocked = this.isLocked()
    return RailGroupState(isLocked, isReserved, isTrainOnRail)
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

    railPosList.map(PosInt::writeToNBT)
        .toNBTTagList()
        .let { tag.setTag("railPosTagList", it) }

    rsPosList.map(PosIntWithKey::writeToNBT)
        .toNBTTagList()
        .let { tag.setTag("rsPosTagList", it) }

    nextRailGroupList.map(UUID::writeToNBT)
        .toNBTTagList()
        .let { tag.setTag("nextRailGroupTagList", it) }

    displayPosList.map(PosInt::writeToNBT)
        .toNBTTagList()
        .let { tag.setTag("displayPosTagList", it) }

    switchSettings.map(SwitchSetting::writeToNBT)
        .toNBTTagList()
        .let { tag.setTag("switchSettingList", it) }

    return tag
}

fun RailGroup.Companion.readFromNBT(nbt: NBTTagCompound): RailGroup {
    val uuid = UUID(nbt.getString("uuid"))
    val name = nbt.getString("name")

    val railPosList = nbt.getTagList("railPosTagList", 10)
        .toList()
        .map(PosInt::readFromNBT)
        .toMutableSet()

    val rsPosList = nbt.getTagList("rsPosTagList", 10)
        .toList()
        .map(PosIntWithKey::readFromNBT)
        .toMutableSet()

    val nextRailGroupList = nbt.getTagList("nextRailGroupTagList", 8)
        .toStringList()
        .map(::UUID)
        .toMutableSet()

    val displayPosList = nbt.getTagList("displayPosTagList", 10)
        .toList()
        .map(PosInt::readFromNBT)
        .toMutableSet()

    val switchSettings = nbt.getTagList("switchSettingList", 10)
        .toList()
        .map(SwitchSetting::readFromNBT)
        .toMutableSet()

    val railGroup = RailGroup(
        uuid,
        name,
        railPosList,
        rsPosList,
        nextRailGroupList,
        displayPosList,
        switchSettings
    )

    return railGroup
}

fun SwitchSetting.writeToNBT(): NBTTagCompound {
    val tag = NBTTagCompound()
    tag.setString("name", name)
    tag.setTag("settingMap", settingMap.map(SettingEntry::writeToNBT).toNBTTagList())
    tag.setTag("switchRsPos", switchRsPos.map(PosInt::writeToNBT).toNBTTagList())

    return tag
}

fun SwitchSetting.Companion.readFromNBT(nbt: NBTTagCompound): SwitchSetting {
    val name = nbt.getString("name")

    val settingMap = nbt.getTagList("settingMap", 10)
        .toList()
        .map(SettingEntry::readFromNBT)
        .toSet()

    val switchRsPos = nbt.getTagList("switchRsPos", 10)
        .toList()
        .map(PosInt::readFromNBT)
        .toSet()

    return SwitchSetting(name, switchRsPos, settingMap)
}

fun SettingEntry.writeToNBT(): NBTTagCompound {
    val tag = NBTTagCompound()
    tag.setString("key", key)
    tag.setBoolean("value", value)
    return tag
}

fun SettingEntry.Companion.readFromNBT(nbt: NBTTagCompound): SettingEntry {
    val key = nbt.getString("key")
    val value = nbt.getBoolean("value")
    return SettingEntry(key, value)
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

    val reservedKey = RailGroupData.getReservedKey(this.uuid)
    this.switchSettings.forEach { switchSetting ->
        if (switchSetting.settingMap.any { reservedKey == it.key }) {
            switchSetting.settingMap.find { reservedKey == it.key }?.let {
                val block = if (it.value) Blocks.redstone_block else Blocks.stained_glass
                switchSetting.switchRsPos.forEach {
                    world.setBlock(it.x, it.y, it.z, block, 14, 3)
                }
            }
        } else if (isTrainOnRail) {
            switchSetting.switchRsPos.forEach {
                val block = world.getBlock(it.x, it.y, it.z)
                world.setBlock(it.x, it.y, it.z, block, 14, 3)
            }
        }
    }

    val block = if (isTrainOnRail) Blocks.redstone_block else Blocks.stained_glass
    this.rsPosList.forEach {
        if (it.key.isEmpty()) {
            world.setBlock(it.x, it.y, it.z, block, 14, 3)
        } else {
            val isReserved = RailGroupData.isReserved(this.uuid, it.key)
            val block = if (isReserved) block else Blocks.redstone_block
            world.setBlock(it.x, it.y, it.z, block, 14, 3)
        }
    }

    if (isTrainOnRail && RailGroupData.hasReleaseFlag(this.uuid)) {
        RailGroupData.unsafeRelease(this.uuid)
    }
}

fun RailGroup.hasSwitch(): Boolean {
    return this.railPosList.mapNotNull { RailCacheData.railMapCache[it] }
        .any { it.railMaps.any { it is RailMapSwitchData } }
}

val TileEntitySignal.signalLevel: Int
    get() = this.javaClass.fields
        .find { it.name == "signalLevel" }
        ?.apply { this.isAccessible = true }
        ?.get(this) as? Int ?: 0

fun List<NBTBase>.toNBTTagList(): NBTTagList {
    return NBTTagList().apply {
        this@toNBTTagList.forEach(::appendTag)
    }
}

fun NBTTagList.toList(): List<NBTTagCompound> {
    return (0 until this.tagCount()).map(::getCompoundTagAt)
}

fun NBTTagList.toStringList(): List<String> {
    return (0 until this.tagCount()).map(::getStringTagAt)
}