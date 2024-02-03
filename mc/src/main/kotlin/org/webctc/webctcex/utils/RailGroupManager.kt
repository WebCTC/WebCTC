package org.webctc.webctcex.utils

import kotlinx.uuid.UUID
import org.webctc.railgroup.RailGroupData

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
    }
}