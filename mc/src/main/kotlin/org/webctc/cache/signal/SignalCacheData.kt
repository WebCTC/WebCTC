package org.webctc.cache.signal

import org.webctc.cache.PosCacheData
import org.webctc.common.types.Pos

class SignalCacheData(mapName: String) : PosCacheData<MutableMap<String, Any?>>(mapName) {
    companion object {
        var signalMapCache = mutableMapOf<Pos, MutableMap<String, Any?>>()
    }

    override fun getMapCache(): MutableMap<Pos, MutableMap<String, Any?>> {
        return signalMapCache
    }

    override val TAG_NAME: String
        get() = "SignalCache"

    override fun fromJson(json: String): MutableMap<String, Any?> {
        return gson.fromJson(json, mutableMapOf<String, Any?>().javaClass)
    }
}