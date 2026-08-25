package org.webctc.webctcex.utils

import org.webctc.cache.tecon.TeConData
import org.webctc.common.types.tecon.TeCon
import org.webctc.common.types.tecon.TeConOperateRequest
import org.webctc.common.types.tecon.TeConOperateResponse
import org.webctc.common.types.tecon.TeConRuntimeState
import org.webctc.tecon.TeConRuntimeManager
import kotlin.uuid.Uuid

class TeConManager {
    companion object {
        @JvmStatic
        fun getTeCon(uuid: String): TeCon? {
            return TeConData.teConList[Uuid.parse(uuid)]
        }

        @JvmStatic
        fun getRuntimeState(uuid: String): TeConRuntimeState? {
            val teCon = getTeCon(uuid) ?: return null
            return TeConRuntimeManager.getRuntimeState(teCon)
        }

        @JvmStatic
        fun operate(uuid: String, request: TeConOperateRequest): TeConOperateResponse? {
            val teCon = getTeCon(uuid) ?: return null
            return TeConRuntimeManager.operate(teCon, request, null)
        }
    }
}