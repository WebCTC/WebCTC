package org.webctc.webctcex.utils

import jp.ngt.rtm.rail.TileEntityLargeRailSwitchCore
import org.webctc.WebCTCCore
import org.webctc.railgroup.RailGroupData
import org.webctc.router.api.isTurning
import kotlin.uuid.Uuid

class RailGroupManager {
    companion object {
        @JvmStatic
        fun setSignal(uuid: String, signal: Int) {
            return RailGroupData.setSignal(Uuid.parse(uuid), signal)
        }

        @JvmStatic
        fun isTrainOnRail(uuid: String): Boolean {
            return RailGroupData.isTrainOnRail(Uuid.parse(uuid))
        }

        @JvmStatic
        fun reserve(uuids: Array<String>, key: String): Boolean {
            return RailGroupData.reserve(uuids.map { Uuid.parse(it) }.toTypedArray(), key)
        }

        @JvmStatic
        fun release(uuids: Array<String>, key: String) {
            RailGroupData.release(uuids.map { Uuid.parse(it) }.toTypedArray(), key)
        }

        @JvmStatic
        fun unsafeRelease(uuid: String) {
            RailGroupData.unsafeRelease(Uuid.parse(uuid))
        }

        @JvmStatic
        fun unsafeRelease(uuids: Array<String>) {
            RailGroupData.unsafeRelease(uuids.map { Uuid.parse(it) }.toTypedArray())
        }

        @JvmStatic
        fun isReserved(uuid: String, key: String): Boolean {
            return RailGroupData.isReserved(Uuid.parse(uuid), key)
        }

        @JvmStatic
        fun isReserved(uuids: Array<String>, key: String): Boolean {
            return RailGroupData.isReserved(uuids.map { Uuid.parse(it) }.toTypedArray(), key)
        }

        @JvmStatic
        fun isLocked(uuid: String, key: String): Boolean {
            return RailGroupData.isLocked(Uuid.parse(uuid), key)
        }

        @JvmStatic
        fun isLocked(uuids: Array<String>, key: String): Boolean {
            return RailGroupData.isLocked(uuids.map { Uuid.parse(it) }.toTypedArray(), key)
        }

        @Deprecated("Use isTurning instead", ReplaceWith("isTurning(uuid)"))
        @JvmStatic
        fun isConverting(uuid: String): Boolean {
            return isTurning(uuid)
        }

        @JvmStatic
        fun isTurning(uuid: String): Boolean {
            return RailGroupData.isTurning(Uuid.parse(uuid))
        }

        @Deprecated("Use isTurning instead", ReplaceWith("isTurning(x, y, z)"))
        @JvmStatic
        fun isConverting(x: Int, y: Int, z: Int): Boolean {
            return isTurning(x, y, z)
        }

        @JvmStatic
        fun isTurning(x: Int, y: Int, z: Int): Boolean {
            return WebCTCCore.INSTANCE.server.entityWorld.getTileEntity(x, y, z)?.let {
                it is TileEntityLargeRailSwitchCore && it.isTurning()
            } == true
        }

        @JvmStatic
        fun getTrainName(uuid: String): String? {
            return RailGroupData.getTrainName(Uuid.parse(uuid))
        }
    }
}