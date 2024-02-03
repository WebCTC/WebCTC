package org.webctc.railgroup

import jp.ngt.rtm.rail.TileEntityLargeRailCore
import kotlinx.uuid.UUID
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.server.MinecraftServer
import net.minecraft.world.WorldSavedData
import org.webctc.common.types.railgroup.Lock
import org.webctc.common.types.railgroup.RailGroup
import org.webctc.common.types.railgroup.RailGroupChain

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
        private val lockList = mutableMapOf<UUID, Lock>()
        private val rgcc = mutableSetOf<RailGroupChain>()

        fun setSignal(uuid: UUID, signal: Int) {
            val world = MinecraftServer.getServer().entityWorld
            findRailGroup(uuid)?.let { rg ->
                rg.railPosList
                    .map { world.getTileEntity(it.x, it.y, it.z) }
                    .filterIsInstance<TileEntityLargeRailCore>()
                    .forEach { it.signal = signal }
            }
        }

        fun isTrainOnRail(uuid: UUID): Boolean {
            return findRailGroup(uuid)?.isTrainOnRail() ?: false
        }

        fun hasReleaseFlag(uuid: UUID): Boolean {
            return lockList[uuid]?.releaseFlag ?: false
        }

        fun reserve(uuids: Array<UUID>, key: String): Boolean {
            val railGroupChain = RailGroupChain(uuids.toLinkedHashSet(), key)

            if (!railGroupChain.canLock()) {
                return false
            }

            railGroupChain.lock()
            rgcc += railGroupChain
            return true
        }

        fun release(uuids: Array<UUID>, key: String) {
            val railGroupChain = RailGroupChain(uuids.toLinkedHashSet(), key)
            if (railGroupChain in rgcc) {
                rgcc -= railGroupChain
                railGroupChain.release()
            }
        }

        fun unsafeRelease(uuid: UUID) {
            lockList -= uuid
        }

        fun isReserved(uuid: UUID, key: String): Boolean {
            return lockList[uuid]?.key == key
        }

        fun isReserved(uuids: Array<UUID>, key: String): Boolean {
            return uuids.any { lockList[it]?.key == key }
        }

        private fun findRailGroup(uuid: UUID): RailGroup? {
            return railGroupList.find { it.uuid == uuid }
        }

        private fun RailGroupChain.canLock(): Boolean {
            return this.chain.mapNotNull(::findRailGroup).all { it.canLock() }
        }

        private fun RailGroupChain.lock() {
            this.chain.mapNotNull(::findRailGroup).forEach {
                lockList[it.uuid] = Lock(this.key)
            }
        }

        private fun RailGroupChain.release() {
            val railGroups = this.chain.mapNotNull(::findRailGroup)
            val freeRGs = railGroups.indexOfFirst { it.isTrainOnRail() }
            if (freeRGs == -1) {
                railGroups.forEach {
                    lockList -= it.uuid
                }
            } else {
                railGroups.subList(0, freeRGs).forEach {
                    lockList -= it.uuid
                }
                railGroups.subList(freeRGs, railGroups.size).forEach {
                    lockList[it.uuid]?.releaseFlag = true
                }
            }
        }

        private fun RailGroup.canLock(): Boolean {
            val isTrainOnRail = this.isTrainOnRail()
            val isReserved = this.uuid in lockList
            val isLocked = lockList.keys
                .mapNotNull(::findRailGroup)
                .any { it.railPosList.any { pos -> pos in this.railPosList } }
            return !(isTrainOnRail || isReserved || isLocked)
        }

        private inline fun <reified T> Array<T>.toLinkedHashSet(): LinkedHashSet<T> {
            return linkedSetOf(*this)
        }
    }
}