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
    }
}