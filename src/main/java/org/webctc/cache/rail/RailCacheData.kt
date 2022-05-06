package org.webctc.cache.rail

import org.webctc.cache.Pos
import org.webctc.cache.PosCacheData
import org.webctc.cache.rail.data.LargeRailData

class RailCacheData(mapName: String) : PosCacheData<LargeRailData>(mapName) {
    companion object {
        var railMapCache = mutableMapOf<Pos, LargeRailData>()
    }

    override fun getMapCache(): MutableMap<Pos, LargeRailData> {
        return railMapCache
    }

    override val TAG_NAME: String
        get() = "RailCache"
}