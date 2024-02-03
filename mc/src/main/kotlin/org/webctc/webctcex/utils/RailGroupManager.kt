package org.webctc.webctcex.utils

import kotlinx.uuid.toKotlinUUID
import org.webctc.railgroup.RailGroupData
import java.util.*

class RailGroupManager {
    companion object {
        @JvmStatic
        fun setSignal(uuid: UUID, signal: Int) {
            RailGroupData.setSignal(uuid.toKotlinUUID(), signal)
        }

        @JvmStatic
        fun setSignal(uuid: String, signal: Int) {
            setSignal(UUID.fromString(uuid), signal)
        }

        @JvmStatic
        fun isTrainOnRail(uuid: UUID): Boolean {
            return RailGroupData.isTrainOnRail(uuid.toKotlinUUID())
        }

        @JvmStatic
        fun isTrainOnRail(uuid: String): Boolean {
            return isTrainOnRail(UUID.fromString(uuid))
        }

        @JvmStatic
        fun reserve(uuid: UUID, key: String) {
            RailGroupData.reserve(uuid.toKotlinUUID(), key)
        }

        @JvmStatic
        fun reserve(uuid: String, key: String) {
            reserve(UUID.fromString(uuid), key)
        }

        @JvmStatic
        fun release(uuid: UUID, key: String) {
            RailGroupData.release(uuid.toKotlinUUID(), key)
        }

        @JvmStatic
        fun release(uuid: String, key: String) {
            release(UUID.fromString(uuid), key)
        }

        @JvmStatic
        fun forceRelease(uuid: UUID) {
            RailGroupData.forceRelease(uuid.toKotlinUUID())
        }

        @JvmStatic
        fun forceRelease(uuid: String) {
            forceRelease(UUID.fromString(uuid))
        }

        @JvmStatic
        fun isReserved(uuid: UUID, key: String): Boolean {
            return RailGroupData.isReserved(uuid.toKotlinUUID(), key)
        }

        @JvmStatic
        fun isReserved(uuid: String, key: String): Boolean {
            return isReserved(UUID.fromString(uuid), key)
        }
    }
}