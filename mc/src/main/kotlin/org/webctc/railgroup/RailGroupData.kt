package org.webctc.railgroup

import jp.ngt.rtm.rail.TileEntityLargeRailCore
import kotlinx.uuid.UUID
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.server.MinecraftServer
import net.minecraft.world.WorldSavedData
import org.webctc.common.types.railgroup.RailGroup


class RailGroupData(mapName: String) : WorldSavedData(mapName) {
    override fun readFromNBT(nbt: NBTTagCompound) {
        railGroupList.clear()
        val tagList = nbt.getTagList("RailGroupData", 10)
        (0 until tagList.tagCount())
            .map { tagList.getCompoundTagAt(it) }
            .mapTo(railGroupList) { RailGroup.readFromNBT(it) }
    }

    override fun writeToNBT(nbt: NBTTagCompound) {
        val tagList = NBTTagList()
        railGroupList.map { it.writeToNBT() }.forEach { tagList.appendTag(it) }
        nbt.setTag("RailGroupData", tagList)
    }

    companion object {
        val railGroupList = mutableListOf<RailGroup>()

        @JvmStatic
        fun setSignal(uuid: UUID, signal: Int) {
            val world = MinecraftServer.getServer().entityWorld
            railGroupList
                .find { it.uuid == uuid }
                ?.let { rg ->
                    rg.railPosList
                        .map { world.getTileEntity(it.x, it.y, it.z) }
                        .filterIsInstance<TileEntityLargeRailCore>()
                        .forEach { it.signal = signal }
                }
        }

        @JvmStatic
        fun setSignal(uuid: String, signal: Int) {
            setSignal(UUID(uuid), signal)
        }

        @JvmStatic
        fun isTrainOnRail(uuid: UUID): Boolean {
            return railGroupList.find { it.uuid == uuid }?.isTrainOnRail() ?: false
        }

        @JvmStatic
        fun isTrainOnRail(uuid: String): Boolean {
            return isTrainOnRail(UUID(uuid))
        }
    }
}