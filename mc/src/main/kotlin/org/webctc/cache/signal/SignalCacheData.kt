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
                .filterIsInstance<TileEntitySignal>()
                .forEach {
                    coreList[PosInt(it.xCoord, it.yCoord, it.zCoord)] = it.toDataClass()
                }
            signalMapCache = coreList
            WebCTCCore.INSTANCE.signalData.markDirty()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}