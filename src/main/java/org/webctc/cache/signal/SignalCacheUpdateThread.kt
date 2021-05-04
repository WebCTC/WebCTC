package org.webctc.cache.signal

import jp.ngt.rtm.electric.BlockSignal
import jp.ngt.rtm.electric.TileEntitySignal
import org.webctc.WebCTCCore
import org.webctc.cache.Pos
import org.webctc.router.api.toMutableMap

class SignalCacheUpdateThread : Thread() {
    override fun run() {
        val world = WebCTCCore.INSTANCE.server.entityWorld
        while (true) {
            try {
                val coreList = SignalCacheData.signalMapCache
                    .filter {
                        !world.chunkProvider.chunkExists(
                            it.key.x / 16,
                            it.key.z / 16
                        ) || world.getBlock(
                            it.key.x,
                            it.key.y,
                            it.key.z
                        ) is BlockSignal
                    }.toMutableMap()
                world.loadedTileEntityList.toMutableList()
                    .filterIsInstance(TileEntitySignal::class.java)
                    .forEach {
                        coreList[Pos(it.xCoord, it.xCoord, it.xCoord)] = it.toMutableMap()
                    }
                SignalCacheData.signalMapCache = coreList
//                WebCTCCore.INSTANCE.signalData.markDirty()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            sleep(1000)
        }
    }
}