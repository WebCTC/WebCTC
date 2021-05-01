package org.webctc.router.api

import express.utils.MediaType
import jp.ngt.ngtlib.util.NGTUtil
import jp.ngt.rtm.electric.TileEntitySignal
import org.webctc.WebCTCCore
import org.webctc.router.WebCTCRouter

class SignalRouter : WebCTCRouter() {
    init {
        get("/") { req, res ->
            res.contentType = MediaType._json.mime

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

    jsonMap["pos"] = arrayOf(this.xCoord, this.yCoord, this.zCoord)
    jsonMap["rotation"] = this.rotation
    jsonMap["signalLevel"] = NGTUtil.getField(TileEntitySignal::class.java, this, "signalLevel")
    jsonMap["modelname"] = this.modelName

    return jsonMap
}