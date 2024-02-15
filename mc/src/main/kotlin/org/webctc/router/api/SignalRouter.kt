package org.webctc.router.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import jp.ngt.ngtlib.util.NGTUtil
import jp.ngt.rtm.electric.TileEntitySignal
import jp.ngt.rtm.modelpack.modelset.ModelSetSignal
import org.webctc.cache.signal.SignalCacheData
import org.webctc.common.types.PosInt
import org.webctc.common.types.signal.SignalData
import org.webctc.router.WebCTCRouter
import org.webctc.signal.SignalStateWS

class SignalRouter : WebCTCRouter() {
    override fun install(application: Route): Route.() -> Unit = {
        get {
            this.call.respond(SignalCacheData.signalMapCache.values)
        }
        get("/signal") {
            val x = this.call.request.queryParameters["x"]?.toIntOrNull()
                ?: return@get this.call.respond(HttpStatusCode.BadRequest)
            val y = this.call.request.queryParameters["y"]?.toIntOrNull()
                ?: return@get this.call.respond(HttpStatusCode.BadRequest)
            val z = this.call.request.queryParameters["z"]?.toIntOrNull()
                ?: return@get this.call.respond(HttpStatusCode.BadRequest)


            val signal: SignalData? = getSignal(x, y, z)

            if (signal == null) {
                this.call.respond(HttpStatusCode.NotFound)
            } else {
                this.call.respond(signal)
            }
        }

        route("/{SignalPos}") {
            get {
                val signalPos = this.call.parameters["SignalPos"]?.toLongOrNull()
                    ?: return@get this.call.respond(HttpStatusCode.BadRequest)
                val signal: SignalData? = getSignal(signalPos)

                if (signal == null) {
                    this.call.respond(HttpStatusCode.NotFound)
                } else {
                    this.call.respond(signal)
                }
            }
        }

        route("/state") {
            webSocket("/ws") {
                val uuids = receiveDeserialized<Set<PosInt>>()
                val signalStateWSSet = uuids.mapNotNull { pos ->
                    val signal = getSignal(pos)
                    if (signal != null) SignalStateWS(signal, this) else null
                }.toSet()
                for (frame in incoming) {
                }
                signalStateWSSet.forEach { it.close() }
            }
        }
    }

    private fun getSignal(pos: PosInt): SignalData? {
        return SignalCacheData.signalMapCache[pos]
    }

    private fun getSignal(serialized: Long): SignalData? {
        val pos = PosInt(serialized)
        return getSignal(pos)
    }

    private fun getSignal(x: Int, y: Int, z: Int): SignalData? {
        return getSignal(PosInt(x, y, z))
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