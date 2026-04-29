package org.webctc.railgroup

import jp.ngt.rtm.rail.TileEntityLargeRailCore
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.server.MinecraftServer
import net.minecraft.world.WorldSavedData
import org.webctc.cache.rail.RailCacheData
import org.webctc.common.types.railgroup.Lock
import org.webctc.common.types.railgroup.RailGroup
import org.webctc.common.types.railgroup.RailGroupChain
import org.webctc.common.types.railgroup.RailGroupFolder
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.uuid.Uuid

class RailGroupData(mapName: String) : WorldSavedData(mapName) {
    override fun readFromNBT(nbt: NBTTagCompound) {
        railGroupList.clear()
        nbt.getTagList("RailGroupData", 10).toList()
            .mapTo(railGroupList, RailGroup::readFromNBT)

        folderList.clear()
        nbt.getTagList("RailGroupFolderData", 10).toList()
            .mapTo(folderList) { RailGroupFolder.readFromNBT(it) }
    }

    override fun writeToNBT(nbt: NBTTagCompound) {
        railGroupList
            .map(RailGroup::writeToNBT)
            .toNBTTagList()
            .let { nbt.setTag("RailGroupData", it) }

        folderList
            .map { it.writeToNBT() }
            .toNBTTagList()
            .let { nbt.setTag("RailGroupFolderData", it) }
    }

    companion object {
        val railGroupList = CopyOnWriteArrayList<RailGroup>()
        val folderList = CopyOnWriteArrayList<RailGroupFolder>()
        private val lockList = mutableMapOf<Uuid, Lock>()
        private val rgcc = mutableSetOf<RailGroupChain>()

        fun setSignal(uuid: Uuid, signal: Int) {
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
            setSignal(Uuid.parse(uuid), signal)
        }

        fun isTrainOnRail(uuid: Uuid): Boolean {
            return findRailGroup(uuid)?.isTrainOnRail() == true
        }

        @JvmStatic
        fun isTrainOnRail(uuid: String): Boolean {
            return isTrainOnRail(Uuid.parse(uuid))
        }

        fun hasReleaseFlag(uuid: Uuid): Boolean {
            return lockList[uuid]?.releaseFlag == true
        }

        @JvmStatic
        fun hasReleaseFlag(uuid: String): Boolean {
            return hasReleaseFlag(Uuid.parse(uuid))
        }

        fun reserve(uuids: Array<Uuid>, key: String): Boolean {
            val railGroupChain = RailGroupChain(uuids.toLinkedHashSet(), key)

            if (!railGroupChain.canLock(key)) {
                return false
            }

            railGroupChain.lock()
            rgcc += railGroupChain
            return true
        }

        @JvmStatic
        fun reserve(uuids: Array<String>, key: String): Boolean {
            return reserve(uuids.map { Uuid.parse(it) }.toTypedArray(), key)
        }

        fun release(uuids: Array<Uuid>, key: String) {
            val railGroupChain = RailGroupChain(uuids.toLinkedHashSet(), key)
            if (railGroupChain in rgcc) {
                rgcc -= railGroupChain
                railGroupChain.release()
            }
        }

        @JvmStatic
        fun release(uuids: Array<String>, key: String) {
            release(uuids.map { Uuid.parse(it) }.toTypedArray(), key)
        }

        fun unsafeRelease(uuid: Uuid) {
            lockList -= uuid
        }

        @JvmStatic
        fun unsafeRelease(uuid: String) {
            unsafeRelease(Uuid.parse(uuid))
        }

        fun unsafeRelease(uuids: Array<Uuid>) {
            lockList -= uuids.toSet()
        }

        @JvmStatic
        fun unsafeRelease(uuids: Array<String>) {
            unsafeRelease(uuids.map { Uuid.parse(it) }.toTypedArray())
        }

        fun isLocked(uuid: Uuid): Boolean {
            return lockList[uuid] != null
        }

        @JvmStatic
        fun isLocked(uuid: String): Boolean {
            return isLocked(Uuid.parse(uuid))
        }

        fun isLocked(uuid: Uuid, key: String): Boolean {
            return lockList[uuid]?.key == key
        }

        @JvmStatic
        fun isLocked(uuid: String, key: String): Boolean {
            return isLocked(Uuid.parse(uuid), key)
        }

        fun isLocked(uuids: Array<Uuid>, key: String): Boolean {
            return uuids.all { isLocked(it, key) }
        }

        @JvmStatic
        fun isLocked(uuids: Array<String>, key: String): Boolean {
            return isLocked(uuids.map { Uuid.parse(it) }.toTypedArray(), key)
        }

        fun isReserved(uuid: Uuid): Boolean {
            val lock = lockList[uuid]
            return lock != null && lock.frozenTime == 0 && !isTurning(uuid)
        }

        @JvmStatic
        fun isReserved(uuid: String): Boolean {
            return isReserved(Uuid.parse(uuid))
        }

        fun isReserved(uuid: Uuid, key: String): Boolean {
            val lock = lockList[uuid]
            return lock?.key == key && lock.frozenTime == 0 && !isTurning(uuid)
        }

        @JvmStatic
        fun isReserved(uuid: String, key: String): Boolean {
            return isReserved(Uuid.parse(uuid), key)
        }

        fun isReserved(uuids: Array<Uuid>, key: String): Boolean {
            return uuids.all { isReserved(it, key) }
        }

        @JvmStatic
        fun isReserved(uuids: Array<String>, key: String): Boolean {
            return isReserved(uuids.map { Uuid.parse(it) }.toTypedArray(), key)
        }

        fun isTurning(uuid: Uuid): Boolean {
            return findRailGroup(uuid)?.let { rg ->
                rg.railPosList
                    .mapNotNull { RailCacheData.railMapCache[it] }
                    .any { it.turning }
            } == true
        }

        @JvmStatic
        fun isTurning(uuid: String): Boolean {
            return isTurning(Uuid.parse(uuid))
        }

        fun getReservedKey(uuid: Uuid): String? {
            return lockList[uuid]?.key
        }

        @JvmStatic
        fun getReservedKey(uuid: String): String? {
            return getReservedKey(Uuid.parse(uuid))
        }

        fun updateLocks() {
            lockList.values.forEach {
                if (it.frozenTime > 0) {
                    it.frozenTime--
                }
            }
        }

        fun getTrainName(uuid: Uuid): String? {
            val railGroup = findRailGroup(uuid) ?: return null

            val isTrainOnRail = railGroup.isTrainOnRail()
            val trainName = if (isTrainOnRail) railGroup.getTrainName() else null
            return trainName
        }

        private fun findRailGroup(uuid: Uuid): RailGroup? {
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