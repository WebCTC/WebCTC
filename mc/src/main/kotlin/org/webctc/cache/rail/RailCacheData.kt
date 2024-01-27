package org.webctc.cache.rail

import io.ktor.server.websocket.*
import jp.ngt.rtm.rail.TileEntityLargeRailCore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import net.minecraft.server.MinecraftServer
import org.webctc.WebCTCCore
import org.webctc.cache.PosCacheData
import org.webctc.common.types.PosInt
import org.webctc.common.types.rail.LargeRailData
import org.webctc.router.api.RailRouter
import org.webctc.router.api.toData

class RailCacheData(mapName: String) : PosCacheData<LargeRailData>(mapName, LargeRailData::class) {
    companion object {
        var railMapCache = mutableMapOf<PosInt, LargeRailData>()
    }

    override fun getMapCache(): MutableMap<PosInt, LargeRailData> {
        return railMapCache
    }

    override val TAG_NAME: String
        get() = "RailCache"

    fun update() {
        val world = WebCTCCore.INSTANCE.server.entityWorld
        try {
            val coreList = railMapCache
                .filter { !world.getChunkFromBlockCoords(it.key.x, it.key.z).isChunkLoaded }
                .toMutableMap().also {
                    it
                        .filter { it.value.isTrainOnRail }
                        .filter { !world.getChunkFromBlockCoords(it.key.x, it.key.z).isChunkLoaded }
                        .forEach { (key) -> world.chunkProvider.loadChunk(key.x / 16, key.z / 16) }
                }.apply {
                    world.loadedTileEntityList
                        .toMutableList()
                        .filterIsInstance<TileEntityLargeRailCore>()
                        .associate { PosInt(it.xCoord, it.yCoord, it.zCoord) to it.toData() }
                        .let { this.putAll(it) }
                }

            val diff = coreList.filter { railMapCache[it.key] != it.value }
            if (diff.isNotEmpty()) {
                RailRouter.connections.forEach {
                    MainScope().launch(Dispatchers.IO) {
                        try {
                            it.session.sendSerialized(diff.values)
                        } catch (e: Exception) {
                            MinecraftServer.getServer().logWarning(e.message)
                        }
                    }
                }
                railMapCache = coreList
                this.markDirty()
            } else if (railMapCache.any { !coreList.contains(it.key) }) {
                railMapCache = coreList
                this.markDirty()
            }
        } catch (e: Exception) {
            MinecraftServer.getServer().logWarning(e.message)
        }
    }
}