package org.webctc.railgroup

import jp.ngt.rtm.rail.TileEntityLargeRailCore
import kotlinx.uuid.UUID
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.server.MinecraftServer
import net.minecraft.world.WorldSavedData
import org.webctc.common.types.railgroup.RailGroup

class RailGroupData(mapName: String) : WorldSavedData(mapName) {
    override fun readFromNBT(nbt: NBTTagCompound) {
        railGroupList.clear()
        nbt.getTagList("RailGroupData", 10).toList()
            .mapTo(railGroupList, RailGroup::readFromNBT)
    }

    override fun writeToNBT(nbt: NBTTagCompound) {
        railGroupList
            .map(RailGroup::writeToNBT)
            .toNBTTagList()
            .let { nbt.setTag("RailGroupData", it) }
    }

    companion object {
        val railGroupList = mutableListOf<RailGroup>()
        val lockList = mutableMapOf<UUID, String>()

        @JvmStatic
        fun setSignal(uuid: UUID, signal: Int) {
            val world = MinecraftServer.getServer().entityWorld
            findRailGroup(uuid)?.let { rg ->
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
            return findRailGroup(uuid)?.isTrainOnRail() ?: false
        }

        @JvmStatic
        fun isTrainOnRail(uuid: String): Boolean {
            return isTrainOnRail(UUID(uuid))
        }

        @JvmStatic
        fun reserve(uuid: UUID, key: String) {
            findRailGroup(uuid)?.let { railGroup ->
                if (!railGroup.isTrainOnRail() &&
                    !lockList.containsKey(uuid) &&
                    lockList.keys
                        .mapNotNull(::findRailGroup)
                        .none { it.railPosList.any { pos -> pos in railGroup.railPosList } }
                ) {
                    lockList[uuid] = key
                }
            }
        }

        @JvmStatic
        fun reserve(uuid: String, key: String) {
            reserve(UUID(uuid), key)
        }

        @JvmStatic
        fun release(uuid: UUID, key: String) {
            findRailGroup(uuid)?.let {
                if (lockList[uuid] == key) {
                    lockList.remove(uuid)
                }
            }
        }

        @JvmStatic
        fun release(uuid: String, key: String) {
            release(UUID(uuid), key)
        }

        @JvmStatic
        fun forceRelease(uuid: UUID) {
            lockList.remove(uuid)
        }

        @JvmStatic
        fun forceRelease(uuid: String) {
            forceRelease(UUID(uuid))
        }

        @JvmStatic
        fun isReserved(uuid: UUID, key: String): Boolean {
            return lockList[uuid] == key
        }

        @JvmStatic
        fun isReserved(uuid: String, key: String): Boolean {
            return isReserved(UUID(uuid), key)
        }

        private fun findRailGroup(uuid: UUID): RailGroup? {
            return railGroupList.find { it.uuid == uuid }
        }
    }
}