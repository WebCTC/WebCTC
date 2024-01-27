package org.webctc.cache.signal

import jp.ngt.rtm.electric.BlockSignal
import jp.ngt.rtm.electric.TileEntitySignal
import org.webctc.WebCTCCore
import org.webctc.cache.PosCacheData
import org.webctc.common.types.PosInt
import org.webctc.common.types.signal.SignalData
import org.webctc.router.api.toDataClass

class SignalCacheData(mapName: String) : PosCacheData<SignalData>(mapName, SignalData::class) {
    companion object {
        var signalMapCache = mutableMapOf<PosInt, SignalData>()
    }

    override fun getMapCache(): MutableMap<PosInt, SignalData> {
        return signalMapCache
    }

    override val TAG_NAME: String
        get() = "SignalCache"

    fun update() {
        val world = WebCTCCore.INSTANCE.server.entityWorld
        try {
            val coreList = signalMapCache
                .filter {
                    !world.getChunkFromBlockCoords(it.key.x, it.key.z).isChunkLoaded
                            || world.getBlock(it.key.x, it.key.y, it.key.z) is BlockSignal
                }.toMutableMap().apply {
                    world.loadedTileEntityList
                        .toMutableList()
                        .filterIsInstance<TileEntitySignal>()
                        .associate { PosInt(it.xCoord, it.yCoord, it.zCoord) to it.toDataClass() }
                        .let { this.putAll(it) }
                }
            signalMapCache = coreList
            this.markDirty()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}