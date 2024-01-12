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
import org.webctc.common.types.Pos
import org.webctc.common.types.signal.SignalData
import org.webctc.router.WebCTCRouter

class SignalRouter : WebCTCRouter() {
    override fun install(application: Route): Route.() -> Unit = {
        get("/") {
            this.call.response.header(HttpHeaders.AccessControlAllowOrigin, "*")
            this.call.respond(SignalCacheData.signalMapCache.values.map(::signalDataFromMap))
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

            if (railCore == null) {
                this.call.respond(HttpStatusCode.NotFound)
            } else {
                this.call.respond(railCore.toMutableMap())
            }
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

fun signalDataFromMap(map: Map<String, Any?>): SignalData {
    val pos = map["pos"] as Array<Int>
    return SignalData(
        Pos(pos[0], pos[1], pos[2]),
        map["rotation"] as Float,
        map["signalLevel"] as Int,
//        signalType = map["signalType"] as Map<Int, Boolean>,
        map["blockDirection"] as Int,
        map["modelName"] as String
    )
}

fun TileEntitySignal.toDataClass(): SignalData {
    return SignalData(
        Pos(this.xCoord, this.yCoord, this.zCoord),
        this.rotation,
        NGTUtil.getField(TileEntitySignal::class.java, this, "signalLevel") as Int,
//        signalType = getSignalTypes(this),
        this.blockDirection,
        this.modelName
    )
}

fun getSignalTypes(tileEntitySignal: TileEntitySignal): Map<Int, Boolean> {
    return ModelSetSignal.parseLightParts(tileEntitySignal.modelSet.config.lights).associate {
        it.signalLevel to true
    }
}