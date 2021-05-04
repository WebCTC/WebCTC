package org.webctc.railcache

import net.minecraft.util.math.BlockPos

class RailCache {
    companion object {
        var railCoreMapCache = mutableMapOf<BlockPos, MutableMap<String, Any?>>()
    }
}