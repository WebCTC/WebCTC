package org.webctc.cache.rail

import org.webctc.cache.Pos
import org.webctc.cache.PosCacheData

class RailCacheData(mapName: String) : PosCacheData<MutableMap<String, Any?>>(mapName) {
    companion object {
        var railMapCache = mutableMapOf<Pos, MutableMap<String, Any?>>()
    }

    override fun getMapCache(): MutableMap<Pos, MutableMap<String, Any?>> {
        return railMapCache
    }

    override val TAG_NAME: String
        get() = "RailCache"
}