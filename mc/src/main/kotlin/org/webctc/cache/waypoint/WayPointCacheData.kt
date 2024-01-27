package org.webctc.cache.waypoint

import org.webctc.cache.CacheData
import org.webctc.common.types.WayPoint

class WayPointCacheData(mapName: String) : CacheData<WayPoint>(mapName, WayPoint::class) {
    companion object {
        var wayPointCache = mutableMapOf<String, WayPoint>()
    }

    override fun getMapCache(): MutableMap<String, WayPoint> {
        return wayPointCache
    }

    override val TAG_NAME: String
        get() = "WayPointCache"
}