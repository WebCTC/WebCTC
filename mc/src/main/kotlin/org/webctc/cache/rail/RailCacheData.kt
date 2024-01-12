package org.webctc.cache.rail

import org.webctc.cache.PosCacheData
import org.webctc.common.types.LargeRailData
import org.webctc.common.types.Pos

class RailCacheData(mapName: String) : PosCacheData<LargeRailData>(mapName) {
    companion object {
        var railMapCache = mutableMapOf<Pos, LargeRailData>()
    }

    override fun getMapCache(): MutableMap<Pos, LargeRailData> {
        return railMapCache
    }

    override val TAG_NAME: String
        get() = "RailCache"

    override fun fromJson(json: String): LargeRailData {
        return gson.fromJson(json, LargeRailData::class.java)
    }
}