package org.webctc.webctcex.utils

import jp.ngt.rtm.rail.TileEntityLargeRailSwitchCore
import kotlinx.uuid.UUID
import org.webctc.WebCTCCore
import org.webctc.railgroup.RailGroupData
import org.webctc.router.api.isConverting

class RailGroupManager {
    companion object {
        @JvmStatic
        fun setSignal(uuid: String, signal: Int) {
            return RailGroupData.setSignal(UUID(uuid), signal)
        }

        @JvmStatic
        fun isTrainOnRail(uuid: String): Boolean {
            return RailGroupData.isTrainOnRail(UUID(uuid))
        }

        @JvmStatic
        fun reserve(uuids: Array<String>, key: String): Boolean {
            return RailGroupData.reserve(uuids.map(::UUID).toTypedArray(), key)
        }

        @JvmStatic
        fun release(uuids: Array<String>, key: String) {
            RailGroupData.release(uuids.map(::UUID).toTypedArray(), key)
        }

        @JvmStatic
        fun unsafeRelease(uuid: String) {
            RailGroupData.unsafeRelease(UUID(uuid))
        }

        @JvmStatic
        fun unsafeRelease(uuids: Array<String>) {
            RailGroupData.unsafeRelease(uuids.map(::UUID).toTypedArray())
        }

        @JvmStatic
        fun isReserved(uuid: String, key: String): Boolean {
            return RailGroupData.isReserved(UUID(uuid), key)
        }

        @JvmStatic
        fun isReserved(uuids: Array<String>, key: String): Boolean {
            return RailGroupData.isReserved(uuids.map(::UUID).toTypedArray(), key)
        }

        @JvmStatic
        fun isLocked(uuid: String, key: String): Boolean {
            return RailGroupData.isLocked(UUID(uuid), key)
        }

        @JvmStatic
        fun isLocked(uuids: Array<String>, key: String): Boolean {
            return RailGroupData.isLocked(uuids.map(::UUID).toTypedArray(), key)
        }

        @JvmStatic
        fun isConverting(uuid: String): Boolean {
            return RailGroupData.isConverting(UUID(uuid))
        }

        @JvmStatic
        fun isConverting(x: Int, y: Int, z: Int): Boolean {
            return WebCTCCore.INSTANCE.server.entityWorld.getTileEntity(x, y, z)?.let {
                it is TileEntityLargeRailSwitchCore && it.isConverting()
            } ?: false
        }
    }
}