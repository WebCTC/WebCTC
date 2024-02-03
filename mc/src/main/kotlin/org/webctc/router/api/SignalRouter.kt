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
import org.webctc.common.types.PosInt
import org.webctc.common.types.signal.SignalData
import org.webctc.router.WebCTCRouter

class SignalRouter : WebCTCRouter() {
    override fun install(application: Route): Route.() -> Unit = {
        get {
            this.call.respond(SignalCacheData.signalMapCache.values)
        }
        get("/signal") {
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
                this.call.respond(railCore.toDataClass())
            }
        }
    }
}

fun TileEntitySignal.toDataClass(): SignalData {
    return SignalData(
        PosInt(this.xCoord, this.yCoord, this.zCoord),
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