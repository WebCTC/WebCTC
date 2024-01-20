package org.webctc.cache.signal

import org.webctc.cache.PosCacheData
import org.webctc.common.types.PosInt
import org.webctc.common.types.signal.SignalData

class SignalCacheData(mapName: String) : PosCacheData<SignalData>(mapName) {
    companion object {
        var signalMapCache = mutableMapOf<PosInt, SignalData>()
    }

    override fun getMapCache(): MutableMap<PosInt, SignalData> {
        return signalMapCache
    }

    override val TAG_NAME: String
        get() = "SignalCache"

    override fun fromJson(json: String): SignalData {
        return gson.fromJson(json, SignalData::class.java)
    }
}