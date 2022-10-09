package org.webctc.cache.rail

import io.ktor.websocket.*
import jp.ngt.rtm.rail.TileEntityLargeRailCore
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.trySendBlocking
import net.minecraft.server.MinecraftServer
import org.webctc.WebCTCCore
import org.webctc.cache.Pos
import org.webctc.router.WebCTCRouter
import org.webctc.router.api.RailRouter
import org.webctc.router.api.toMutableMap
import kotlin.concurrent.thread

class RailCacheUpdate {
    fun execute() {
        val world = WebCTCCore.INSTANCE.server.entityWorld
        try {
            val coreList = RailCacheData.railMapCache
                .filter { !world.chunkProvider.chunkExists(it.key.x / 16, it.key.z / 16) }.toMutableMap()
            coreList
                .filter { it.value.isTrainOnRail }
                .filter { !world.chunkProvider.chunkExists(it.key.x / 16, it.key.z / 16) }
                .forEach { (key) ->
                    world.chunkProvider.loadChunk(key.x / 16, key.z / 16)
                }
            world.loadedTileEntityList.toMutableList().filterIsInstance(TileEntityLargeRailCore::class.java).forEach {
                coreList[Pos(it.xCoord, it.yCoord, it.zCoord)] = it.toMutableMap()
            }
            val diff = coreList.filter { RailCacheData.railMapCache[it.key] != it.value }
            if (diff.isNotEmpty()) {
                val json = WebCTCRouter.gson.toJson(diff.values)
                RailRouter.connections.forEach {
                    thread {
                        try {
                            it.session.outgoing.trySendBlocking(Frame.Text(json)).onFailure { e ->
                                MinecraftServer.getServer().logWarning(e?.message)
                            }
                        } catch (e: Exception) {
                            MinecraftServer.getServer().logWarning(e.message)
                        }
                    }
                }
                RailCacheData.railMapCache = coreList
                WebCTCCore.INSTANCE.railData.markDirty()
            }
        } catch (e: Exception) {
            MinecraftServer.getServer().logWarning(e.message)
        }
    }
}