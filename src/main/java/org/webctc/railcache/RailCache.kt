package org.webctc.railcache

import net.minecraft.util.math.BlockPos
import org.webctc.WebCTCCore

class RailCache {
    companion object {
        var railCoreMapCache = mutableMapOf<BlockPos, MutableMap<String, Any?>>()
            set(value) {
                field = value
                WebCTCCore.INSTANCE.railData.markDirty()
            }
    }
}