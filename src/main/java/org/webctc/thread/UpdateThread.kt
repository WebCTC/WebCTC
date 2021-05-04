package org.webctc.thread

import jp.ngt.rtm.rail.BlockLargeRailCore
import jp.ngt.rtm.rail.TileEntityLargeRailCore
import net.minecraft.util.math.BlockPos
import net.minecraft.world.gen.ChunkProviderServer
import org.webctc.WebCTCCore
import org.webctc.railcache.RailCache
import org.webctc.router.api.toBlockPos
import org.webctc.router.api.toMutableMap

class UpdateThread {
    companion object {
        var thread: Thread? = null
        fun start() {
            thread = Thread {
                val world = WebCTCCore.INSTANCE.server.entityWorld
                while (true) {
                    val coreList = RailCache.railCoreMapCache
                        .filter {
                            !(world.chunkProvider as ChunkProviderServer).chunkExists(
                                it.key.x / 16,
                                it.key.z / 16
                            ) || world.getBlockState(
                                BlockPos(
                                    it.key.x,
                                    it.key.y,
                                    it.key.z
                                )
                            ).block is BlockLargeRailCore
                        }.toMutableMap()
                    world.loadedTileEntityList.toMutableList()
                        .filterIsInstance(TileEntityLargeRailCore::class.java)
                        .forEach {
                            coreList[it.startPoint.toBlockPos()] = it.toMutableMap()
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