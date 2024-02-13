package org.webctc.router.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import jp.ngt.ngtlib.util.NGTUtil
import jp.ngt.rtm.electric.TileEntitySignal
import jp.ngt.rtm.modelpack.modelset.ModelSetSignal
import org.webctc.cache.signal.SignalCacheData
import org.webctc.common.types.PosInt
import org.webctc.common.types.signal.SignalData
import org.webctc.common.types.signal.SignalState
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

            route("/state") {
                get {
                    val signalPos = this.call.parameters["SignalPos"]?.toLongOrNull()
                        ?: return@get this.call.respond(HttpStatusCode.BadRequest)
                    val signal: SignalData? = getSignal(signalPos)

                    if (signal == null) {
                        this.call.respond(HttpStatusCode.NotFound)
                    } else {
                        val signalState = SignalState(signal.signalLevel)
                        this.call.respond(signalState)
                    }
                }


                webSocket("/ws") {
                    val signalPos = this.call.parameters["SignalPos"]?.toLongOrNull()
                        ?: return@webSocket this.close(
                            CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Invalid signal position")
                        )
                    val signal = getSignal(signalPos) ?: return@webSocket this.close(
                        CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Signal not found")
                    )

                    val signalStateWS = SignalStateWS(signal, this)
                    for (frame in incoming) {
                    }
                    signalStateWS.close()
                }
            }
        }
    }

    private fun getSignal(serialized: Long): SignalData? {
        val pos = PosInt(serialized)
        return getSignal(pos.x, pos.y, pos.z)
    }

    private fun getSignal(x: Int, y: Int, z: Int): SignalData? {
        return SignalCacheData.signalMapCache[PosInt(x, y, z)]
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