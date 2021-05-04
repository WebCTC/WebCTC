package org.webctc.cache.rail

import org.webctc.cache.CacheData
import org.webctc.cache.Pos

class RailCacheData(mapName: String) : CacheData(mapName) {
    companion object {
        var railMapCache = mutableMapOf<Pos, MutableMap<String, Any?>>()
    }

    override fun getMapCache(): MutableMap<Pos, MutableMap<String, Any?>> {
        return railMapCache
    }

    override val TAG_NAME: String
        get() = "RailCache"
}