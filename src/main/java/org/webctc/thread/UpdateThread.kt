package org.webctc.thread

import jp.ngt.rtm.rail.BlockLargeRailCore
import jp.ngt.rtm.rail.TileEntityLargeRailCore
import org.webctc.WebCTCCore
import org.webctc.railcache.RailCache
import org.webctc.router.api.toMutableMap
import org.webctc.router.api.toPos

class UpdateThread {
    companion object {
        var thread: Thread? = null
        fun start() {
            thread = Thread {
                val world = WebCTCCore.INSTANCE.server.entityWorld
                while (true) {
                    val coreList = RailCache.railCoreMapCache
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
                    RailCache.railCoreMapCache = coreList
                    WebCTCCore.INSTANCE.railData.markDirty()
                    Thread.sleep(5000)
                }
            }
            thread?.start()
        }

        fun stop() {
            thread?.stop()
        }
    }
}