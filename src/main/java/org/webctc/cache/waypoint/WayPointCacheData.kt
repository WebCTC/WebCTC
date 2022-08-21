package org.webctc.cache.waypoint

import org.webctc.cache.CacheData

class WayPointCacheData(mapName: String) : CacheData<WayPoint>(mapName) {
    companion object {
        var wayPointCache = mutableMapOf<String, WayPoint>()
    }

    override fun getMapCache(): MutableMap<String, WayPoint> {
        return wayPointCache
    }

    override val TAG_NAME: String
        get() = "WayPointCache"
}