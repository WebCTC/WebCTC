package org.webctc.router.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import jp.ngt.ngtlib.util.NGTUtil
import jp.ngt.rtm.electric.TileEntitySignal
import jp.ngt.rtm.modelpack.modelset.ModelSetSignal
import org.webctc.WebCTCCore
import org.webctc.cache.signal.SignalCacheData
import org.webctc.router.WebCTCRouter

class SignalRouter : WebCTCRouter() {
    override fun install(application: Route): Route.() -> Unit = {
        get("/") {
            this.call.response.header(HttpHeaders.AccessControlAllowOrigin, "*")
            this.call.respond { SignalCacheData.signalMapCache.values }
        }
        get("/signal") {
            this.call.response.header(HttpHeaders.AccessControlAllowOrigin, "*")
            val x = this.call.request.queryParameters["x"]?.toIntOrNull()
            val y = this.call.request.queryParameters["y"]?.toIntOrNull()
            val z = this.call.request.queryParameters["z"]?.toIntOrNull()
            var railCore: TileEntitySignal? = null
            if (x != null && y != null && z != null) {
                railCore = WebCTCCore.INSTANCE.server.entityWorld.getTileEntity(x, y, z) as? TileEntitySignal
            }
            this.call.respond { railCore?.toMutableMap() }
        }
    }
}

fun TileEntitySignal.toMutableMap(): MutableMap<String, Any?> {
    val jsonMap = mutableMapOf<String, Any?>()

    try {
        jsonMap["pos"] = arrayOf(this.xCoord, this.yCoord, this.zCoord)
        jsonMap["rotation"] = this.rotation
        jsonMap["signalLevel"] = NGTUtil.getField(TileEntitySignal::class.java, this, "signalLevel")
//        jsonMap["signalType"] = getSignalTypes(this)
        jsonMap["blockDirection"] = this.blockDirection
        jsonMap["modelName"] = this.modelName
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return jsonMap
}

fun getSignalTypes(tileEntitySignal: TileEntitySignal): Map<Int, Boolean> {
    return ModelSetSignal.parseLightParts(tileEntitySignal.modelSet.config.lights).associate {
        it.signalLevel to true
    }
}