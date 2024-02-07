package org.webctc.railgroup

import jp.ngt.rtm.rail.TileEntityLargeRailCore
import kotlinx.uuid.UUID
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.server.MinecraftServer
import net.minecraft.world.WorldSavedData
import org.webctc.cache.rail.RailCacheData
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

            if (!railGroupChain.canLock(key)) {
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

        fun unsafeRelease(uuids: Array<UUID>) {
            lockList -= uuids.toSet()
        }

        fun isLocked(uuid: UUID, key: String): Boolean {
            return lockList[uuid]?.key == key
        }

        fun isLocked(uuids: Array<UUID>, key: String): Boolean {
            return uuids.all { isLocked(it, key) }
        }

        fun isReserved(uuid: UUID, key: String): Boolean {
            val lock = lockList[uuid]
            return lock?.key == key && lock.frozenTime == 0 && !isConverting(uuid)
        }

        fun isReserved(uuids: Array<UUID>, key: String): Boolean {
            return uuids.all { isReserved(it, key) }
        }

        private fun isConverting(uuid: UUID): Boolean {
            return findRailGroup(uuid)?.let { rg ->
                return rg.railPosList
                    .mapNotNull { RailCacheData.railMapCache[it] }
                    .any { it.converting }
            } ?: false
        }

        fun getReservedKey(uuid: UUID): String? {
            return lockList[uuid]?.key
        }

        fun updateLocks() {
            lockList.values.forEach {
                if (it.frozenTime > 0) {
                    it.frozenTime--
                }
            }
        }

        private fun findRailGroup(uuid: UUID): RailGroup? {
            return railGroupList.find { it.uuid == uuid }
        }

        private fun RailGroupChain.canLock(key: String): Boolean {
            return this.chain.mapNotNull(::findRailGroup).all { it.canLock(key) }
        }

        private fun RailGroupChain.lock() {
            this.chain.mapNotNull(::findRailGroup).forEach {
                val hasSwitch = it.hasSwitch()
                val isKeyEquals = lockList[it.uuid]?.key == this.key
                val finishFrozen = isKeyEquals && lockList[it.uuid]?.frozenTime == 0
                val frozenTime =
                    if (hasSwitch) (if (isKeyEquals) lockList[it.uuid]!!.frozenTime else 20) else 0
                lockList[it.uuid] = Lock(this.key, frozenTime)
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

        private fun RailGroup.canLock(key: String): Boolean {
            val isTrainOnRail = this.isTrainOnRail()
            val isReserved = this.uuid in lockList && lockList[this.uuid]!!.key != key
            val isLocked = lockList
                .filterValues { it.key != key }
                .keys
                .mapNotNull(::findRailGroup)
                .any { it.railPosList.any { pos -> pos in this.railPosList } }
            return !(isTrainOnRail || isReserved || isLocked)
        }

        private inline fun <reified T> Array<T>.toLinkedHashSet(): LinkedHashSet<T> {
            return linkedSetOf(*this)
        }
    }
}