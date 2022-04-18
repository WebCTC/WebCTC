package org.webctc.cache.signal

import org.webctc.cache.Pos
import org.webctc.cache.PosCacheData

class SignalCacheData(mapName: String) : PosCacheData<MutableMap<String, Any?>>(mapName) {
    companion object {
        var signalMapCache = mutableMapOf<Pos, MutableMap<String, Any?>>()
    }

    override fun getMapCache(): MutableMap<Pos, MutableMap<String, Any?>> {
        return signalMapCache
    }

    override val TAG_NAME: String
        get() = "SignalCache"
}