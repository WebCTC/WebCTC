package org.webctc.cache.signal

import org.webctc.cache.CacheData
import org.webctc.cache.Pos

class SignalCacheData(mapName: String) : CacheData(mapName) {
    companion object {
        var signalMapCache = mutableMapOf<Pos, MutableMap<String, Any?>>()
    }

    override fun getMapCache(): MutableMap<Pos, MutableMap<String, Any?>> {
        return signalMapCache
    }

    override val TAG_NAME: String
        get() = "SignalCache"
}