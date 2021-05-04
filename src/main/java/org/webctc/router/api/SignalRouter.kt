package org.webctc.router.api

import express.utils.MediaType
import jp.ngt.ngtlib.util.NGTUtil
import jp.ngt.rtm.electric.TileEntitySignal
import jp.ngt.rtm.modelpack.modelset.ModelSetSignal
import org.webctc.WebCTCCore
import org.webctc.cache.signal.SignalCacheData
import org.webctc.router.WebCTCRouter

class SignalRouter : WebCTCRouter() {
    init {
        get("/") { req, res ->
            res.contentType = MediaType._json.mime
            res.setHeader("Access-Control-Allow-Origin", "*")
            res.send(gson.toJson(SignalCacheData.signalMapCache.map { it.value }))
        }
        get("/signal") { req, res ->
            res.contentType = MediaType._json.mime
            res.setHeader("Access-Control-Allow-Origin", "*")
            val x = req.getQuery("x").toIntOrNull()
            val y = req.getQuery("y").toIntOrNull()
            val z = req.getQuery("z").toIntOrNull()
            var railCore: TileEntitySignal? = null
            if (x != null && y != null && z != null) {
                railCore = WebCTCCore.INSTANCE.server.entityWorld.getTileEntity(x, y, z) as? TileEntitySignal
            }
            res.send(
                gson.toJson(
                    railCore?.toMutableMap()
                )
            )
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
        jsonMap["modelname"] = this.modelName
    } catch (e: Exception) {
    }

    return jsonMap
}

fun getSignalTypes(tileEntitySignal: TileEntitySignal): Map<Int, Boolean> {
    return ModelSetSignal.parseLightParts(tileEntitySignal.modelSet.config.lights).associate {
        it.signalLevel to true
    }
}