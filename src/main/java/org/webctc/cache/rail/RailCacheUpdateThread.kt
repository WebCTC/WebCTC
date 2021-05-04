package org.webctc.cache.rail

import jp.ngt.rtm.rail.BlockLargeRailCore
import jp.ngt.rtm.rail.TileEntityLargeRailCore
import org.webctc.WebCTCCore
import org.webctc.router.api.toMutableMap
import org.webctc.router.api.toPos

class RailCacheUpdateThread : Thread() {
    override fun run() {
        val world = WebCTCCore.INSTANCE.server.entityWorld
        while (true) {
            try {
                val coreList = RailCacheData.railMapCache
                    .filter {
                        !world.chunkProvider.chunkExists(
                            it.key.x / 16,
                            it.key.z / 16
                        ) || world.getBlock(
                            it.key.x,
                            it.key.y,
                            it.key.z
                        ) is BlockLargeRailCore
                    }.toMutableMap()
                world.loadedTileEntityList.toMutableList()
                    .filterIsInstance(TileEntityLargeRailCore::class.java)
                    .forEach {
                        coreList[it.startPoint.toPos()] = it.toMutableMap()
                    }
                RailCacheData.railMapCache = coreList
//                WebCTCCore.INSTANCE.railData.markDirty()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            sleep(1000)
        }
    }
}