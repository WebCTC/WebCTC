package org.webctc.router.api

import express.utils.MediaType
import jp.ngt.ngtlib.util.NGTUtil
import jp.ngt.rtm.electric.TileEntitySignal
import jp.ngt.rtm.modelpack.modelset.ModelSetSignal
import org.webctc.WebCTCCore
import org.webctc.router.WebCTCRouter

class SignalRouter : WebCTCRouter() {
    init {
        get("/") { req, res ->
            res.contentType = MediaType._json.mime
            res.setHeader("Access-Control-Allow-Origin", "*")

            val signals = WebCTCCore.INSTANCE.server.entityWorld.loadedTileEntityList
                .filterIsInstance(TileEntitySignal::class.java)
            res.send(
                gson.toJson(signals.map(TileEntitySignal::toMutableMap))
            )
        }
    }
}

fun TileEntitySignal.toMutableMap(): MutableMap<String, Any?> {
    val jsonMap = mutableMapOf<String, Any?>()

    jsonMap["pos"] = arrayOf(this.x, this.y, this.z)
    jsonMap["rotation"] = this.rotation
    jsonMap["signalLevel"] = NGTUtil.getField(TileEntitySignal::class.java, this, "signalLevel")
//    jsonMap["signalType"] = getSignalTypes(this)
    jsonMap["blockDirection"] = this.blockDirection
    jsonMap["modelname"] = this.resourceState.resourceName

    return jsonMap
}

fun getSignalTypes(tileEntitySignal: TileEntitySignal): Map<Int, Boolean> {
    return ModelSetSignal.parseLightParts(tileEntitySignal.resourceState.resourceSet.config.lights).associate {
        it.signalLevel to true
    }
}