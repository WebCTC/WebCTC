package org.webctc.router.api

import express.utils.MediaType
import jp.ngt.rtm.rail.TileEntityLargeRailBase
import jp.ngt.rtm.rail.TileEntityLargeRailCore
import jp.ngt.rtm.rail.util.RailPosition
import net.minecraft.util.MathHelper
import net.minecraft.world.World
import org.webctc.WebCTCCore
import org.webctc.railcache.RailCache
import org.webctc.router.WebCTCRouter

class RailRouter : WebCTCRouter() {
    init {
        get("/") { req, res ->
            res.contentType = MediaType._json.mime
            res.setHeader("Access-Control-Allow-Origin", "*")
            res.send(gson.toJson(RailCache.railCoreMapCache.map { it.value.toMutableMap() }))
        }
        get("/rail") { req, res ->
            res.contentType = MediaType._json.mime
            val x = req.getQuery("x").toIntOrNull()
            val y = req.getQuery("y").toIntOrNull()
            val z = req.getQuery("z").toIntOrNull()
            var railCore: TileEntityLargeRailCore? = null
            if (x != null && y != null && z != null) {
                railCore = WebCTCCore.INSTANCE.server.entityWorld.getTileEntity(x, y, z) as? TileEntityLargeRailCore
            }
            res.send(
                gson.toJson(
                    railCore?.toMutableMap()
                )
            )
        }
    }
}

fun TileEntityLargeRailCore.toMutableMap(): MutableMap<String, Any?> {
    val jsonMap = mutableMapOf<String, Any?>()

    jsonMap["pos"] = this.startPoint
    jsonMap["isTrainOnRail"] = this.isTrainOnRail
    jsonMap["railMaps"] = this.getNeighborRailCores()
    jsonMap["isCache"] = false

    return jsonMap
}

fun TileEntityLargeRailCore.getNeighborRailCores(): List<Map<String, Any>> {
    return this.allRailMaps.map {
        mapOf(
            "startRP" to it.startRP,
            "endRP" to it.endRP,
            "length" to it.length,
            "neighborRailCores" to mapOf(
                "startPR" to it.startRP.getNeighborRail(this.worldObj)?.startPoint,
                "endRP" to it.endRP.getNeighborRail(this.worldObj)?.startPoint
            )
        )
    }
}

fun RailPosition.getNeighborRail(world: World): TileEntityLargeRailCore? {
    return (world.getTileEntity(
        MathHelper.floor_double(this.posX + RailPosition.REVISION[this.direction.toInt()][0]),
        this.blockY,
        MathHelper.floor_double(this.posZ + RailPosition.REVISION[this.direction.toInt()][1])
    ) as? TileEntityLargeRailBase)?.railCore
}

fun IntArray.toPos() = RailCache.Pos(this[0], this[1], this[2])